package com.smartheal.views;

import com.smartheal.api.ApiClient;
import com.smartheal.dao.HistoryDAO;
import com.smartheal.models.CostEstimation;
import com.smartheal.utils.FileExporter;
import com.smartheal.utils.NotificationHelper;
import com.smartheal.utils.ReportFormatter;
import com.smartheal.utils.UsageTracker;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class CostEstimatorView extends BorderPane {
    private final ApiClient apiClient;
    private final ObservableList<String> treatments;
    private final HistoryDAO historyDAO;
    private Integer currentUserId = null;
    
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
    private ComboBox<String> treatmentCombo;
    private ComboBox<String> hospitalTypeCombo;
    private Button estimateButton;
    private ScrollPane resultsPane;
    private ProgressIndicator loadingIndicator;
    private CostEstimation currentEstimation;
    private Button exportButton;
    private Button copyButton;

    public CostEstimatorView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.treatments = FXCollections.observableArrayList();
        this.historyDAO = new HistoryDAO();

        VBox headerBox = createHeader();
        setTop(headerBox);

        HBox mainContent = new HBox(25);
        mainContent.setPadding(new Insets(25));
        mainContent.setStyle("-fx-background-color: #F0F9FF;");

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        mainContent.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        setCenter(mainContent);

        loadTreatments();
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(12);
        headerBox.getStyleClass().add("header-section");
        headerBox.setPadding(new Insets(25, 25, 20, 25));

        Label titleLabel = new Label("💰 Treatment Cost Estimator");
        titleLabel.getStyleClass().add("header-title");

        Label descriptionLabel = new Label(
            "Get estimated costs for medical treatments and procedures at different types of hospitals in India"
        );
        descriptionLabel.getStyleClass().add("header-description");

        headerBox.getChildren().addAll(titleLabel, descriptionLabel, new DisclaimerBanner());

        return headerBox;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(20);
        leftPanel.getStyleClass().add("panel");
        leftPanel.setMinWidth(380);
        leftPanel.setMaxWidth(450);
        VBox.setVgrow(leftPanel, Priority.ALWAYS);

        Label panelTitle = new Label("Estimate Parameters");
        panelTitle.getStyleClass().add("panel-title");

        VBox treatmentBox = new VBox(5);
        Label treatmentLabel = new Label("Treatment or Procedure");
        treatmentLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        treatmentCombo = new ComboBox<>(treatments);
        treatmentCombo.setPromptText("Select a treatment");
        treatmentCombo.setPrefHeight(35);
        treatmentBox.getChildren().addAll(treatmentLabel, treatmentCombo);

        VBox hospitalBox = new VBox(5);
        Label hospitalLabel = new Label("Hospital Type");
        hospitalLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        hospitalTypeCombo = new ComboBox<>();
        hospitalTypeCombo.getItems().addAll("Government Hospital", "Semi-Private Hospital", "Private Hospital");
        hospitalTypeCombo.setValue("Government Hospital");
        hospitalTypeCombo.setPrefHeight(35);
        hospitalBox.getChildren().addAll(hospitalLabel, hospitalTypeCombo);

        estimateButton = new Button("💰 Estimate Cost");
        estimateButton.getStyleClass().addAll("button", "button-primary");
        estimateButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        estimateButton.setPrefHeight(50);
        estimateButton.setPrefWidth(Double.MAX_VALUE);
        estimateButton.setCursor(javafx.scene.Cursor.HAND);
        estimateButton.setTooltip(new javafx.scene.control.Tooltip("Get AI-powered cost estimation for selected treatment and hospital type"));
        estimateButton.setOnAction(e -> estimateCost());
        estimateButton.setDisable(true);

        treatmentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            estimateButton.setDisable(newVal == null || hospitalTypeCombo.getValue() == null);
        });

        hospitalTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            estimateButton.setDisable(newVal == null || treatmentCombo.getValue() == null);
        });

        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: #F0F9FF; -fx-padding: 15; -fx-background-radius: 5;");
        Label infoLabel = new Label("ℹ Cost estimates are based on average data and may vary significantly based on location, hospital reputation, doctor experience, and individual patient needs.");
        infoLabel.setWrapText(true);
        infoLabel.setFont(Font.font("System", 11));
        infoBox.getChildren().add(infoLabel);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);

        leftPanel.getChildren().addAll(panelTitle, treatmentBox, hospitalBox, estimateButton, infoBox, loadingIndicator);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.getStyleClass().add("panel");
        rightPanel.setMinWidth(600);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);

        // Action buttons for results
        HBox actionButtonsBox = new HBox(8);
        actionButtonsBox.setAlignment(Pos.CENTER_RIGHT);
        actionButtonsBox.setPadding(new Insets(10, 0, 10, 0));
        
        exportButton = new Button("💾 Export");
        exportButton.setStyle("-fx-background-color: linear-gradient(to bottom, #3B82F6, #60A5FA); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        exportButton.setTooltip(new Tooltip("Export cost estimation to file"));
        exportButton.setOnAction(e -> exportEstimation());
        exportButton.setDisable(true);
        
        copyButton = new Button("📋 Copy");
        copyButton.setStyle("-fx-background-color: linear-gradient(to bottom, #6366F1, #818CF8); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        copyButton.setTooltip(new Tooltip("Copy estimation to clipboard"));
        copyButton.setOnAction(e -> copyEstimation());
        copyButton.setDisable(true);
        
        actionButtonsBox.getChildren().addAll(exportButton, copyButton);
        
        resultsPane = new ScrollPane();
        resultsPane.setFitToWidth(true);
        resultsPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(resultsPane, Priority.ALWAYS);

        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(50));
        Label emptyLabel = new Label("No Estimate Yet");
        emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label emptyDesc = new Label("Select a treatment and hospital type, then click \"Estimate Cost\" to see pricing information");
        emptyDesc.setFont(Font.font("System", 12));
        emptyDesc.setTextFill(Color.GRAY);
        emptyDesc.setWrapText(true);
        emptyState.getChildren().addAll(emptyLabel, emptyDesc);
        resultsPane.setContent(emptyState);

        rightPanel.getChildren().addAll(actionButtonsBox, resultsPane);

        return rightPanel;
    }

    private void loadTreatments() {
        loadingIndicator.setVisible(true);
        new Thread(() -> {
            try {
                if (!apiClient.isBackendAvailable()) {
                    Platform.runLater(() -> {
                        showError("Backend Unavailable", 
                            "Cannot connect to Python backend server.\nPlease ensure the Python backend is running (python app.py).");
                        loadingIndicator.setVisible(false);
                    });
                    return;
                }
                
                var treatmentList = apiClient.getTreatments();
                Platform.runLater(() -> {
                    if (treatmentList == null || treatmentList.isEmpty()) {
                        showError("No Data", "No treatments data received from backend.");
                        loadingIndicator.setVisible(false);
                        return;
                    }
                    treatments.setAll(treatmentList);
                    loadingIndicator.setVisible(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Connection Error", 
                        "Failed to load treatments:\n" + e.getMessage() + 
                        "\n\nPlease check your connection and try again.");
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void estimateCost() {
        String treatmentType = treatmentCombo.getValue();
        String hospitalType = hospitalTypeCombo.getValue();

        if (treatmentType == null || hospitalType == null) {
            showError("Missing Information", "Please select a treatment type and hospital type");
            return;
        }

        // Convert display name to API value
        String apiHospitalType = hospitalType.equals("Government Hospital") ? "government" :
                                hospitalType.equals("Semi-Private Hospital") ? "semi-private" : "private";

        estimateButton.setDisable(true);
        loadingIndicator.setVisible(true);
        
        NotificationHelper.showInfoNotification(
            (StackPane) getScene().getRoot(),
            "Estimating cost for " + treatmentType + "... Please wait."
        );

        new Thread(() -> {
            try {
                CostEstimation estimation = apiClient.estimateCost(treatmentType, apiHospitalType);
                Platform.runLater(() -> {
                    displayCostEstimation(estimation);
                    UsageTracker.incrementCostsEstimated();
                    
                    // Save to history if user is logged in
                    if (currentUserId != null) {
                        try {
                            historyDAO.saveCostEstimationHistory(
                                currentUserId,
                                treatmentType,
                                hospitalType,
                                estimation.getAverageCost(),
                                estimation.getMinCost(),
                                estimation.getMaxCost()
                            );
                        } catch (Exception e) {
                            System.err.println("Failed to save cost estimation history: " + e.getMessage());
                        }
                    }
                    
                    NotificationHelper.showSuccessNotification(
                        (StackPane) getScene().getRoot(),
                        "Cost estimation complete! Average: ₹" + String.format("%.2f", estimation.getAverageCost())
                    );
                    estimateButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    String errorMsg = "Failed to estimate cost:\n" + e.getMessage();
                    
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout") ||
                        e.getMessage().contains("Cannot connect")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Check: http://localhost:5000/api/estimate-cost in browser\n" +
                                   "3. Verify Python backend server started successfully (python app.py)";
                    }
                    
                    showError("Error", errorMsg);
                    estimateButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Unexpected Error", 
                        "An unexpected error occurred:\n" + e.getMessage() + 
                        "\n\nPlease try again or restart the application.");
                    estimateButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void displayCostEstimation(CostEstimation estimation) {
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));

        // Header with cost - Beautiful gradient
        VBox headerBox = new VBox(10);
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #E0F2FE, #F0FDFA, #E0F2FE); -fx-padding: 25; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.15), 8, 0, 0, 3);");
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label treatmentNameLabel = new Label("💰 " + estimation.getTreatmentName());
        treatmentNameLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        treatmentNameLabel.setTextFill(Color.rgb(15, 118, 110));
        Label hospitalTypeLabel = new Label(estimation.getHospitalType());
        hospitalTypeLabel.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-padding: 8 18; -fx-background-radius: 20; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.3), 4, 0, 0, 2);");
        titleBox.getChildren().addAll(treatmentNameLabel, hospitalTypeLabel);

        HBox costBox = new HBox(10);
        costBox.setAlignment(Pos.CENTER_RIGHT);
        VBox costDetails = new VBox(5);
        Label costLabel = new Label("Average Cost");
        costLabel.setFont(Font.font("System", 12));
        costLabel.setTextFill(Color.GRAY);
        Label averageCostLabel = new Label(formatCurrency(estimation.getAverageCost()));
        averageCostLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        averageCostLabel.setTextFill(Color.rgb(15, 118, 110));
        averageCostLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.3), 5, 0, 0, 2);");
        costDetails.getChildren().addAll(costLabel, averageCostLabel);
        costBox.getChildren().add(costDetails);

        HBox minMaxBox = new HBox(10);
        minMaxBox.setSpacing(10);
        VBox minBox = new VBox(5);
        minBox.setAlignment(Pos.CENTER);
        minBox.setStyle("-fx-background-color: linear-gradient(to bottom, #F0FDF4, #D1FAE5); -fx-border-color: #22C55E; -fx-border-width: 2; -fx-padding: 18; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(34, 197, 94, 0.2), 4, 0, 0, 2);");
        Label minLabel = new Label("📉 Minimum Cost");
        minLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        minLabel.setTextFill(Color.rgb(5, 95, 70));
        Label minCostLabel = new Label(formatCurrency(estimation.getMinCost()));
        minCostLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        minCostLabel.setTextFill(Color.rgb(5, 95, 70));
        minBox.getChildren().addAll(minLabel, minCostLabel);

        VBox maxBox = new VBox(5);
        maxBox.setAlignment(Pos.CENTER);
        maxBox.setStyle("-fx-background-color: linear-gradient(to bottom, #FEF2F2, #FEE2E2); -fx-border-color: #EF4444; -fx-border-width: 2; -fx-padding: 18; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.2), 4, 0, 0, 2);");
        Label maxLabel = new Label("📈 Maximum Cost");
        maxLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        maxLabel.setTextFill(Color.rgb(153, 27, 27));
        Label maxCostLabel = new Label(formatCurrency(estimation.getMaxCost()));
        maxCostLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        maxCostLabel.setTextFill(Color.rgb(153, 27, 27));
        maxBox.getChildren().addAll(maxLabel, maxCostLabel);

        minMaxBox.getChildren().addAll(minBox, maxBox);
        HBox.setHgrow(minBox, Priority.ALWAYS);
        HBox.setHgrow(maxBox, Priority.ALWAYS);

        headerBox.getChildren().addAll(titleBox, costBox, minMaxBox);

        // Factors - Colorful design
        VBox factorsBox = new VBox(10);
        factorsBox.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 2; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);");
        Label factorsTitle = new Label("📋 Factors Affecting Cost");
        factorsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        factorsTitle.setTextFill(Color.rgb(15, 118, 110));
        factorsBox.getChildren().add(factorsTitle);

        String[] factorColors = {"#0F766E", "#14B8A6", "#22D3EE", "#3B82F6", "#6366F1"};
        for (int i = 0; i < estimation.getFactors().size(); i++) {
            var factor = estimation.getFactors().get(i);
            HBox factorBox = new HBox(12);
            factorBox.setStyle("-fx-background-color: linear-gradient(to right, #F0F9FF, #F0FDFA); -fx-border-color: " + factorColors[i % factorColors.length] + "; -fx-border-width: 2; -fx-padding: 18; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.1), 4, 0, 0, 2);");
            Label numLabel = new Label(String.valueOf(i + 1));
            numLabel.setStyle("-fx-background-color: linear-gradient(to bottom, " + factorColors[i % factorColors.length] + ", " + (i < factorColors.length - 1 ? factorColors[i + 1] : factorColors[0]) + "); -fx-text-fill: white; -fx-padding: 8 14; -fx-background-radius: 50; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
            numLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            VBox factorContent = new VBox(3);
            Label factorName = new Label(factor.getName());
            factorName.setFont(Font.font("System", FontWeight.BOLD, 13));
            Label factorImpact = new Label(factor.getImpact());
            factorImpact.setWrapText(true);
            factorImpact.setFont(Font.font("System", 11));
            factorContent.getChildren().addAll(factorName, factorImpact);
            factorBox.getChildren().addAll(numLabel, factorContent);
            factorsBox.getChildren().add(factorBox);
        }

        // Disclaimer
        VBox disclaimerBox = new VBox(5);
        disclaimerBox.setStyle("-fx-background-color: #FEF2F2; -fx-border-color: #FCA5A5; -fx-border-width: 2; -fx-padding: 15; -fx-background-radius: 10;");
        Label disclaimerTitle = new Label("⚠ Important Note");
        disclaimerTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        disclaimerTitle.setTextFill(Color.rgb(185, 28, 28));
        Label disclaimerText = new Label(estimation.getDisclaimer());
        disclaimerText.setWrapText(true);
        disclaimerText.setFont(Font.font("System", 11));
        disclaimerBox.getChildren().addAll(disclaimerTitle, disclaimerText);

        contentBox.getChildren().addAll(headerBox, factorsBox, disclaimerBox);
        resultsPane.setContent(contentBox);
        currentEstimation = estimation;
        exportButton.setDisable(false);
        copyButton.setDisable(false);
    }

    private void exportEstimation() {
        if (currentEstimation == null) {
            showError("No Estimation", "No cost estimation to export.");
            return;
        }

        String report = ReportFormatter.formatCostEstimation(currentEstimation);
        FileExporter.exportToFile(
            report,
            "cost_estimation",
            "Export Cost Estimation",
            (Stage) getScene().getWindow()
        );
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Estimation exported successfully!"
        );
    }

    private void copyEstimation() {
        if (currentEstimation == null) {
            showError("No Estimation", "No cost estimation to copy.");
            return;
        }

        String report = ReportFormatter.formatCostEstimation(currentEstimation);
        FileExporter.copyToClipboard(report);
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Estimation copied to clipboard!"
        );
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
}


package com.smartheal.views;

import com.smartheal.api.ApiClient;
import com.smartheal.models.Disease;
import com.smartheal.models.TestRecommendation;
import com.smartheal.utils.FileExporter;
import com.smartheal.utils.JSONExporter;
import com.smartheal.utils.NotificationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestLookupView extends BorderPane {
    private final ApiClient apiClient;
    private final ObservableList<Disease> allDiseases;
    private final ObjectMapper objectMapper;
    private ListView<Disease> diseaseList;
    private TextField searchField;
    private ScrollPane resultsPane;
    private ProgressIndicator loadingIndicator;
    private Disease selectedDisease;
    private TestRecommendation currentRecommendation;
    private Button exportButton;
    private Button exportJsonButton;
    private Button copyButton;
    private Button printButton;
    private HBox statsBox;

    public TestLookupView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.allDiseases = FXCollections.observableArrayList();
        this.objectMapper = new ObjectMapper();

        VBox headerBox = createHeader();
        setTop(headerBox);

        HBox mainContent = new HBox(15);
        mainContent.setPadding(new Insets(15));
        mainContent.setStyle("-fx-background-color: #F0F9FF;");

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        mainContent.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        // Make panels responsive
        leftPanel.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.40));
        rightPanel.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.60));

        setCenter(mainContent);

        loadDiseases();
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.getStyleClass().add("header-section");
        headerBox.setPadding(new Insets(15, 20, 12, 20));

        Label titleLabel = new Label("🧪 Test Recommendations");
        titleLabel.getStyleClass().add("header-title");

        Label descriptionLabel = new Label(
            "Discover which medical tests are recommended for specific conditions and which ones to avoid"
        );
        descriptionLabel.getStyleClass().add("header-description");

        headerBox.getChildren().addAll(titleLabel, descriptionLabel, new com.smartheal.views.DisclaimerBanner());

        return headerBox;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(12);
        leftPanel.getStyleClass().add("panel");
        leftPanel.setMinWidth(350);
        leftPanel.setMaxWidth(450);
        VBox.setVgrow(leftPanel, Priority.ALWAYS);

        Label panelTitle = new Label("Select a Condition");
        panelTitle.getStyleClass().add("panel-title");

        searchField = new TextField();
        searchField.setPromptText("🔍 Search conditions...");
        searchField.setPrefHeight(36);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterDiseases(newVal));

        diseaseList = new ListView<>();
        diseaseList.setCellFactory(param -> new DiseaseCell());
        VBox.setVgrow(diseaseList, Priority.ALWAYS);
        diseaseList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedDisease = newVal;
                loadTestRecommendations(newVal.getId());
            }
        });

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);

        leftPanel.getChildren().addAll(panelTitle, searchField, diseaseList, loadingIndicator);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(12);
        rightPanel.getStyleClass().add("panel");
        rightPanel.setMinWidth(500);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);

        // Header with action buttons
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label panelTitle = new Label("Test Recommendations");
        panelTitle.getStyleClass().add("panel-title");
        HBox.setHgrow(panelTitle, Priority.ALWAYS);
        
        // Action buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        exportButton = new Button("📄 Export TXT");
        exportButton.getStyleClass().add("button");
        exportButton.setStyle("-fx-background-color: linear-gradient(to bottom, #3B82F6, #60A5FA); -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12;");
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> exportToTXT());
        
        exportJsonButton = new Button("📊 Export JSON");
        exportJsonButton.getStyleClass().add("button");
        exportJsonButton.setStyle("-fx-background-color: linear-gradient(to bottom, #8B5CF6, #A78BFA); -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12;");
        exportJsonButton.setDisable(true);
        exportJsonButton.setOnAction(e -> exportToJSON());
        
        copyButton = new Button("📋 Copy");
        copyButton.getStyleClass().add("button");
        copyButton.setStyle("-fx-background-color: linear-gradient(to bottom, #10B981, #34D399); -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12;");
        copyButton.setDisable(true);
        copyButton.setOnAction(e -> copyToClipboard());
        
        printButton = new Button("🖨️ Print");
        printButton.getStyleClass().add("button");
        printButton.setStyle("-fx-background-color: linear-gradient(to bottom, #6B7280, #9CA3AF); -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12;");
        printButton.setDisable(true);
        printButton.setOnAction(e -> printRecommendations());
        
        buttonBox.getChildren().addAll(exportButton, exportJsonButton, copyButton, printButton);
        headerBox.getChildren().addAll(panelTitle, buttonBox);
        
        // Stats box
        statsBox = new HBox(10);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(5, 0, 5, 0));
        statsBox.setVisible(false);

        resultsPane = new ScrollPane();
        resultsPane.setFitToWidth(true);
        resultsPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(resultsPane, Priority.ALWAYS);

        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(50));
        Label emptyLabel = new Label("No Condition Selected");
        emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label emptyDesc = new Label("Select a medical condition from the list to view recommended and unnecessary tests");
        emptyDesc.setFont(Font.font("System", 12));
        emptyDesc.setTextFill(Color.GRAY);
        emptyDesc.setWrapText(true);
        emptyState.getChildren().addAll(emptyLabel, emptyDesc);
        resultsPane.setContent(emptyState);

        rightPanel.getChildren().addAll(headerBox, statsBox, resultsPane);

        return rightPanel;
    }

    private void loadDiseases() {
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
                
                var diseases = apiClient.getDiseases();
                Platform.runLater(() -> {
                    if (diseases == null || diseases.isEmpty()) {
                        showError("No Data", "No diseases data received from backend.");
                        loadingIndicator.setVisible(false);
                        return;
                    }
                    allDiseases.setAll(diseases);
                    diseaseList.setItems(allDiseases);
                    loadingIndicator.setVisible(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    String errorMsg = "Failed to load diseases:\n" + e.getMessage();
                    
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout") ||
                        e.getMessage().contains("Cannot connect")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Check: http://localhost:5000/api/diseases in browser\n" +
                                   "3. Verify Python backend server started successfully (python app.py)";
                    }
                    
                    showError("Connection Error", errorMsg);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Unexpected Error", 
                        "An unexpected error occurred:\n" + e.getMessage() + 
                        "\n\nPlease try again or restart the application.");
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void filterDiseases(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            diseaseList.setItems(allDiseases);
            return;
        }

        String lowerSearch = searchTerm.toLowerCase();
        ObservableList<Disease> filtered = allDiseases.stream()
            .filter(d -> d.getName().toLowerCase().contains(lowerSearch))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        diseaseList.setItems(filtered);
    }

    private void loadTestRecommendations(String diseaseId) {
        loadingIndicator.setVisible(true);
        exportButton.setDisable(true);
        exportJsonButton.setDisable(true);
        copyButton.setDisable(true);
        printButton.setDisable(true);
        statsBox.setVisible(false);
        
        new Thread(() -> {
            try {
                TestRecommendation recommendation = apiClient.getTestRecommendations(diseaseId);
                Platform.runLater(() -> {
                    currentRecommendation = recommendation;
                    displayTestRecommendations(recommendation);
                    updateStats(recommendation);
                    exportButton.setDisable(false);
                    exportJsonButton.setDisable(false);
                    copyButton.setDisable(false);
                    printButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    String errorMsg = "Failed to load test recommendations:\n" + e.getMessage();
                    
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout") ||
                        e.getMessage().contains("Cannot connect")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Check: http://localhost:5000/api/tests/" + diseaseId + " in browser\n" +
                                   "3. Verify Python backend server started successfully (python app.py)";
                    }
                    
                    showError("Error", errorMsg);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Unexpected Error", 
                        "An unexpected error occurred:\n" + e.getMessage() + 
                        "\n\nPlease try again or restart the application.");
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void displayTestRecommendations(TestRecommendation recommendation) {
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));

        // Header
        VBox headerBox = new VBox(5);
        headerBox.setStyle("-fx-background-color: #E0F2FE; -fx-padding: 15; -fx-background-radius: 10;");
        Label diseaseNameLabel = new Label(recommendation.getDiseaseName());
        diseaseNameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        Label reasoningLabel = new Label(recommendation.getReasoning());
        reasoningLabel.setWrapText(true);
        reasoningLabel.setFont(Font.font("System", 12));
        headerBox.getChildren().addAll(diseaseNameLabel, reasoningLabel);

        // Recommended Tests
        VBox recommendedBox = new VBox(10);
        recommendedBox.setStyle("-fx-background-color: #F0FDF4; -fx-border-color: #22C55E; -fx-border-width: 2; -fx-padding: 15; -fx-background-radius: 10;");
        Label recommendedTitle = new Label("✓ Recommended Tests");
        recommendedTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        recommendedTitle.setTextFill(Color.rgb(22, 163, 74));
        recommendedBox.getChildren().add(recommendedTitle);

        if (recommendation.getRecommendedTests().isEmpty()) {
            Label noTestsLabel = new Label("No specific tests recommended");
            noTestsLabel.setFont(Font.font("System", 12));
            recommendedBox.getChildren().add(noTestsLabel);
        } else {
            Accordion recommendedAccordion = new Accordion();
            for (var test : recommendation.getRecommendedTests()) {
                TitledPane pane = new TitledPane();
                pane.setText(test.getName() + " - " + test.getCostRange());
                VBox testContent = new VBox(8);
                testContent.setPadding(new Insets(10));
                Label descLabel = new Label("Description: " + test.getDescription());
                descLabel.setWrapText(true);
                Label purposeLabel = new Label("Purpose: " + test.getPurpose());
                purposeLabel.setWrapText(true);
                if (test.getPreparation() != null && !test.getPreparation().isEmpty()) {
                    Label prepLabel = new Label("Preparation: " + test.getPreparation());
                    prepLabel.setWrapText(true);
                    testContent.getChildren().addAll(descLabel, purposeLabel, prepLabel);
                } else {
                    testContent.getChildren().addAll(descLabel, purposeLabel);
                }
                pane.setContent(testContent);
                recommendedAccordion.getPanes().add(pane);
            }
            recommendedBox.getChildren().add(recommendedAccordion);
        }

        // Tests to Avoid
        VBox avoidBox = new VBox(10);
        avoidBox.setStyle("-fx-background-color: #FEF2F2; -fx-border-color: #EF4444; -fx-border-width: 2; -fx-padding: 15; -fx-background-radius: 10;");
        Label avoidTitle = new Label("✗ Tests to Avoid or Unnecessary");
        avoidTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        avoidTitle.setTextFill(Color.rgb(239, 68, 68));
        avoidBox.getChildren().add(avoidTitle);

        if (recommendation.getTestsToAvoid().isEmpty()) {
            Label noTestsLabel = new Label("No tests specifically flagged to avoid");
            noTestsLabel.setFont(Font.font("System", 12));
            avoidBox.getChildren().add(noTestsLabel);
        } else {
            Accordion avoidAccordion = new Accordion();
            for (var test : recommendation.getTestsToAvoid()) {
                TitledPane pane = new TitledPane();
                pane.setText(test.getName() + " - " + test.getCostRange());
                VBox testContent = new VBox(8);
                testContent.setPadding(new Insets(10));
                Label descLabel = new Label("Why to avoid: " + test.getDescription());
                descLabel.setWrapText(true);
                Label purposeLabel = new Label("Reasoning: " + test.getPurpose());
                purposeLabel.setWrapText(true);
                testContent.getChildren().addAll(descLabel, purposeLabel);
                pane.setContent(testContent);
                avoidAccordion.getPanes().add(pane);
            }
            avoidBox.getChildren().add(avoidAccordion);
        }

        contentBox.getChildren().addAll(headerBox, recommendedBox, avoidBox);
        resultsPane.setContent(contentBox);
    }
    
    private void updateStats(TestRecommendation recommendation) {
        statsBox.getChildren().clear();
        statsBox.setVisible(true);
        
        int recommendedCount = recommendation.getRecommendedTests().size();
        int avoidCount = recommendation.getTestsToAvoid().size();
        
        // Recommended tests stat
        VBox recommendedStat = createStatCard("✓ Recommended", String.valueOf(recommendedCount), "#22C55E");
        // Avoid tests stat
        VBox avoidStat = createStatCard("✗ To Avoid", String.valueOf(avoidCount), "#EF4444");
        // Total tests stat
        VBox totalStat = createStatCard("📊 Total", String.valueOf(recommendedCount + avoidCount), "#3B82F6");
        
        statsBox.getChildren().addAll(recommendedStat, avoidStat, totalStat);
    }
    
    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(3);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 8;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));
        
        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("System", 10));
        descLabel.setTextFill(Color.GRAY);
        
        card.getChildren().addAll(valueLabel, descLabel);
        return card;
    }
    
    private void exportToTXT() {
        if (currentRecommendation == null) {
            showError("No Data", "No test recommendations to export.");
            return;
        }
        
        StringBuilder content = new StringBuilder();
        content.append("MEDICAL TEST RECOMMENDATIONS\n");
        content.append("=".repeat(50)).append("\n\n");
        content.append("Condition: ").append(currentRecommendation.getDiseaseName()).append("\n");
        content.append("Reasoning: ").append(currentRecommendation.getReasoning()).append("\n\n");
        
        content.append("RECOMMENDED TESTS (").append(currentRecommendation.getRecommendedTests().size()).append(")\n");
        content.append("-".repeat(50)).append("\n");
        for (var test : currentRecommendation.getRecommendedTests()) {
            content.append("\n• ").append(test.getName()).append("\n");
            content.append("  Cost: ").append(test.getCostRange()).append("\n");
            content.append("  Description: ").append(test.getDescription()).append("\n");
            content.append("  Purpose: ").append(test.getPurpose()).append("\n");
            if (test.getPreparation() != null && !test.getPreparation().isEmpty()) {
                content.append("  Preparation: ").append(test.getPreparation()).append("\n");
            }
        }
        
        content.append("\n\nTESTS TO AVOID (").append(currentRecommendation.getTestsToAvoid().size()).append(")\n");
        content.append("-".repeat(50)).append("\n");
        for (var test : currentRecommendation.getTestsToAvoid()) {
            content.append("\n• ").append(test.getName()).append("\n");
            content.append("  Cost: ").append(test.getCostRange()).append("\n");
            content.append("  Why to avoid: ").append(test.getDescription()).append("\n");
            content.append("  Reasoning: ").append(test.getPurpose()).append("\n");
        }
        
        FileExporter.exportToFile(
            content.toString(),
            "test_recommendations_" + currentRecommendation.getDiseaseName().replaceAll("[^a-zA-Z0-9]", "_"),
            "Export Test Recommendations",
            (Stage) getScene().getWindow()
        );
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Test recommendations exported successfully!"
        );
    }
    
    private void exportToJSON() {
        if (currentRecommendation == null) {
            showError("No Data", "No test recommendations to export.");
            return;
        }
        
        try {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("timestamp", new java.util.Date().toString());
            exportData.put("diseaseId", currentRecommendation.getDiseaseId());
            exportData.put("diseaseName", currentRecommendation.getDiseaseName());
            exportData.put("reasoning", currentRecommendation.getReasoning());
            exportData.put("recommendedTests", currentRecommendation.getRecommendedTests());
            exportData.put("testsToAvoid", currentRecommendation.getTestsToAvoid());
            
            JSONExporter.exportToJSON(
                exportData,
                "test_recommendations_" + currentRecommendation.getDiseaseName().replaceAll("[^a-zA-Z0-9]", "_"),
                "Export Test Recommendations as JSON",
                (Stage) getScene().getWindow()
            );
            
            NotificationHelper.showSuccessNotification(
                (StackPane) getScene().getRoot(),
                "JSON exported successfully!"
            );
        } catch (Exception e) {
            showError("Export Error", "Failed to export JSON: " + e.getMessage());
        }
    }
    
    private void copyToClipboard() {
        if (currentRecommendation == null) {
            showError("No Data", "No test recommendations to copy.");
            return;
        }
        
        StringBuilder content = new StringBuilder();
        content.append("Test Recommendations for: ").append(currentRecommendation.getDiseaseName()).append("\n\n");
        content.append("Recommended Tests:\n");
        for (var test : currentRecommendation.getRecommendedTests()) {
            content.append("• ").append(test.getName()).append(" (").append(test.getCostRange()).append(")\n");
        }
        content.append("\nTests to Avoid:\n");
        for (var test : currentRecommendation.getTestsToAvoid()) {
            content.append("• ").append(test.getName()).append(" (").append(test.getCostRange()).append(")\n");
        }
        
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content.toString());
        clipboard.setContent(clipboardContent);
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Copied to clipboard!"
        );
    }
    
    private void printRecommendations() {
        if (currentRecommendation == null) {
            showError("No Data", "No test recommendations to print.");
            return;
        }
        
        // Use JavaFX Print API
        try {
            javafx.print.PrinterJob printerJob = javafx.print.PrinterJob.createPrinterJob();
            if (printerJob != null && printerJob.showPrintDialog((Stage) getScene().getWindow())) {
                // Create printable content
                VBox printContent = new VBox(15);
                printContent.setPadding(new Insets(40));
                printContent.setStyle("-fx-font-size: 12px;");
                
                Label title = new Label("Medical Test Recommendations");
                title.setFont(Font.font("System", FontWeight.BOLD, 18));
                Label disease = new Label("Condition: " + currentRecommendation.getDiseaseName());
                disease.setFont(Font.font("System", FontWeight.BOLD, 14));
                Label reasoning = new Label("Reasoning: " + currentRecommendation.getReasoning());
                reasoning.setWrapText(true);
                
                printContent.getChildren().addAll(title, disease, reasoning);
                
                // Print the content
                boolean success = printerJob.printPage(printContent);
                if (success) {
                    printerJob.endJob();
                    NotificationHelper.showSuccessNotification(
                        (StackPane) getScene().getRoot(),
                        "Print job sent!"
                    );
                } else {
                    showError("Print Error", "Failed to print document.");
                }
            }
        } catch (Exception e) {
            showError("Print Error", "Failed to print: " + e.getMessage());
        }
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

    private static class DiseaseCell extends ListCell<Disease> {
        @Override
        protected void updateItem(Disease disease, boolean empty) {
            super.updateItem(disease, empty);
            if (empty || disease == null) {
                setGraphic(null);
            } else {
                VBox cellBox = new VBox(5);
                cellBox.setPadding(new Insets(10));
                cellBox.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 5;");

                HBox headerBox = new HBox(10);
                Label nameLabel = new Label(disease.getName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                Label severityBadge = new Label(disease.getSeverity());
                severityBadge.setStyle("-fx-background-color: #E0F2FE; -fx-padding: 3 10; -fx-background-radius: 10;");
                headerBox.getChildren().addAll(nameLabel, severityBadge);

                Label descLabel = new Label(disease.getDescription());
                descLabel.setFont(Font.font("System", 11));
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(350);

                cellBox.getChildren().addAll(headerBox, descLabel);
                setGraphic(cellBox);
            }
        }
    }
}


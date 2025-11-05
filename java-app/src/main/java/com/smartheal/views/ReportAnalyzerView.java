package com.smartheal.views;

import com.smartheal.api.ApiClient;
import com.smartheal.dao.HistoryDAO;
import com.smartheal.models.BloodParameter;
import com.smartheal.models.ReportAnalysis;
import com.smartheal.utils.FileExporter;
import com.smartheal.utils.NotificationHelper;
import com.smartheal.utils.ReportFormatter;
import com.smartheal.utils.UsageTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ReportAnalyzerView extends BorderPane {
    private final ApiClient apiClient;
    private final Map<String, TextField> inputFields;
    private final HistoryDAO historyDAO;
    private final ObjectMapper objectMapper;
    private Integer currentUserId = null;
    
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
    private Button analyzeButton;
    private ScrollPane resultsPane;
    private ProgressIndicator loadingIndicator;
    private ReportAnalysis currentAnalysis;
    private Button exportButton;
    private Button copyButton;
    private Button printButton;

    private static final String[][] BLOOD_PARAMETERS = {
        {"hemoglobin", "Hemoglobin", "g/dL", "12-16", "false"},
        {"wbc", "WBC Count", "cells/µL", "4000-11000", "false"},
        {"platelets", "Platelet Count", "cells/µL", "150000-450000", "false"},
        {"rbc", "RBC Count", "million cells/µL", "4.5-5.5", "false"},
        {"bloodSugar", "Blood Sugar (Fasting)", "mg/dL", "70-100", "true"},
        {"cholesterol", "Total Cholesterol", "mg/dL", "<200", "true"}
    };

    public ReportAnalyzerView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.inputFields = new HashMap<>();
        this.historyDAO = new HistoryDAO();
        this.objectMapper = new ObjectMapper();

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
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(12);
        headerBox.getStyleClass().add("header-section");
        headerBox.setPadding(new Insets(25, 25, 20, 25));

        Label titleLabel = new Label("📊 Medical Report Analyzer");
        titleLabel.getStyleClass().add("header-title");

        Label descriptionLabel = new Label(
            "Input your blood test values to receive educational feedback comparing them to standard reference ranges"
        );
        descriptionLabel.getStyleClass().add("header-description");

        headerBox.getChildren().addAll(titleLabel, descriptionLabel, new DisclaimerBanner());

        return headerBox;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.getStyleClass().add("panel");
        leftPanel.setMinWidth(400);
        leftPanel.setMaxWidth(480);
        VBox.setVgrow(leftPanel, Priority.ALWAYS);

        Label panelTitle = new Label("Blood Test Parameters");
        panelTitle.getStyleClass().add("panel-title");
        
        // Add image upload option
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E5E7EB;");
        
        Label uploadLabel = new Label("📷 Upload Report Image");
        uploadLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        uploadLabel.setTextFill(Color.web("#0F766E"));
        
        Button uploadImageButton = new Button("📤 Upload & Scan Image");
        uploadImageButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #8B5CF6, #A78BFA); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 15; " +
            "-fx-cursor: hand;"
        );
        uploadImageButton.setPrefWidth(Double.MAX_VALUE);
        uploadImageButton.setTooltip(new Tooltip("Upload a photo of your blood test report to automatically extract values"));
        uploadImageButton.setOnAction(e -> uploadAndScanImage());
        
        Label orLabel = new Label("OR");
        orLabel.setFont(Font.font("System", 11));
        orLabel.setTextFill(Color.GRAY);
        orLabel.setAlignment(Pos.CENTER);
        orLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label manualLabel = new Label("Enter Values Manually");
        manualLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        manualLabel.setTextFill(Color.GRAY);

        VBox inputsBox = new VBox(15);
        for (String[] param : BLOOD_PARAMETERS) {
            String key = param[0];
            String label = param[1];
            String unit = param[2];
            String normalRange = param[3];
            boolean optional = Boolean.parseBoolean(param[4]);

            VBox paramBox = new VBox(5);
            HBox labelBox = new HBox(5);
            labelBox.setAlignment(Pos.CENTER_LEFT);
            Label paramLabel = new Label(label + (optional ? " (optional)" : ""));
            paramLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            Label rangeLabel = new Label("Normal: " + normalRange);
            rangeLabel.setFont(Font.font("System", 10));
            rangeLabel.setTextFill(Color.GRAY);
            labelBox.getChildren().addAll(paramLabel, rangeLabel);

            HBox inputBox = new HBox(5);
            TextField inputField = new TextField();
            inputField.setPromptText("Enter " + label.toLowerCase());
            inputField.setPrefHeight(35);
            inputFields.put(key, inputField);

            Label unitLabel = new Label(unit);
            unitLabel.setFont(Font.font("System", 11));
            unitLabel.setTextFill(Color.GRAY);
            unitLabel.setMinWidth(80);

            inputBox.getChildren().addAll(inputField, unitLabel);
            HBox.setHgrow(inputField, Priority.ALWAYS);

            paramBox.getChildren().addAll(labelBox, inputBox);
            inputsBox.getChildren().add(paramBox);
        }

        analyzeButton = new Button("📊 Analyze Report");
        analyzeButton.getStyleClass().addAll("button", "button-primary");
        analyzeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        analyzeButton.setPrefHeight(50);
        analyzeButton.setPrefWidth(Double.MAX_VALUE);
        analyzeButton.setCursor(javafx.scene.Cursor.HAND);
        analyzeButton.setTooltip(new javafx.scene.control.Tooltip("Analyze blood test results with AI-powered insights and recommendations"));
        analyzeButton.setOnAction(e -> analyzeReport());

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);

        leftPanel.getChildren().addAll(
            panelTitle, 
            separator,
            uploadLabel,
            uploadImageButton,
            orLabel,
            manualLabel,
            inputsBox, 
            analyzeButton, 
            loadingIndicator
        );

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
        exportButton.setTooltip(new Tooltip("Export analysis report to file"));
        exportButton.setOnAction(e -> exportReport());
        exportButton.setDisable(true);
        
        copyButton = new Button("📋 Copy");
        copyButton.setStyle("-fx-background-color: linear-gradient(to bottom, #6366F1, #818CF8); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        copyButton.setTooltip(new Tooltip("Copy report to clipboard"));
        copyButton.setOnAction(e -> copyReport());
        copyButton.setDisable(true);
        
        printButton = new Button("🖨️ Print");
        printButton.setStyle("-fx-background-color: linear-gradient(to bottom, #8B5CF6, #A78BFA); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        printButton.setTooltip(new Tooltip("Print analysis report"));
        printButton.setOnAction(e -> printReport());
        printButton.setDisable(true);
        
        actionButtonsBox.getChildren().addAll(exportButton, copyButton, printButton);
        
        resultsPane = new ScrollPane();
        resultsPane.setFitToWidth(true);
        resultsPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(resultsPane, Priority.ALWAYS);

        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(50));
        Label emptyLabel = new Label("No Analysis Yet");
        emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label emptyDesc = new Label("Enter your blood test values and click \"Analyze Report\" to see educational insights");
        emptyDesc.setFont(Font.font("System", 12));
        emptyDesc.setTextFill(Color.GRAY);
        emptyDesc.setWrapText(true);
        emptyState.getChildren().addAll(emptyLabel, emptyDesc);
        resultsPane.setContent(emptyState);

        rightPanel.getChildren().addAll(actionButtonsBox, resultsPane);

        return rightPanel;
    }

    private void analyzeReport() {
        Map<String, Double> reportData = new HashMap<>();
        boolean hasRequired = true;
        String missingField = null;

        for (String[] param : BLOOD_PARAMETERS) {
            String key = param[0];
            String label = param[1];
            boolean optional = Boolean.parseBoolean(param[4]);
            TextField field = inputFields.get(key);
            String value = field.getText().trim();

            if (!value.isEmpty()) {
                try {
                    double numValue = Double.parseDouble(value);
                    // Validate reasonable ranges
                    if (numValue < 0) {
                        showError("Invalid Input", "Please enter a positive number for " + label);
                        return;
                    }
                    // Basic range validation for common parameters
                    if (key.equals("hemoglobin") && numValue > 30) {
                        showError("Invalid Input", "Hemoglobin value seems unusually high. Please verify: " + numValue);
                        return;
                    }
                    if (key.equals("wbc") && numValue > 100000) {
                        showError("Invalid Input", "WBC count seems unusually high. Please verify: " + numValue);
                        return;
                    }
                    reportData.put(key, numValue);
                } catch (NumberFormatException e) {
                    showError("Invalid Input", "Please enter a valid number for " + label);
                    return;
                }
            } else if (!optional) {
                hasRequired = false;
                if (missingField == null) {
                    missingField = label;
                }
            }
        }

        if (!hasRequired) {
            showError("Missing Required Fields", 
                "Please fill in all required blood test parameters.\n" +
                "Missing: " + missingField + " and possibly others.");
            return;
        }

        if (reportData.isEmpty()) {
            showError("No Data", "Please enter at least one blood test value");
            return;
        }

        analyzeButton.setDisable(true);
        loadingIndicator.setVisible(true);
        
        NotificationHelper.showInfoNotification(
            (StackPane) getScene().getRoot(),
            "Analyzing blood report... Please wait."
        );

        new Thread(() -> {
            try {
                ReportAnalysis analysis = apiClient.analyzeReport(reportData);
                javafx.application.Platform.runLater(() -> {
                    displayAnalysis(analysis);
                    UsageTracker.incrementReportsAnalyzed();
                    
                    // Save to history if user is logged in
                    if (currentUserId != null) {
                        try {
                            String reportDataJson = objectMapper.writeValueAsString(reportData);
                            String analysisResult = objectMapper.writeValueAsString(analysis);
                            String flaggedParams = String.join(", ", analysis.getFlaggedParameters());
                            
                            historyDAO.saveReportHistory(
                                currentUserId, 
                                reportDataJson, 
                                analysisResult, 
                                flaggedParams, 
                                analysis.getOverallStatus()
                            );
                        } catch (Exception e) {
                            System.err.println("Failed to save report history: " + e.getMessage());
                        }
                    }
                    
                    NotificationHelper.showSuccessNotification(
                        (StackPane) getScene().getRoot(),
                        "Report analyzed successfully! " + analysis.getFlaggedParameters().size() + " parameter(s) flagged."
                    );
                    analyzeButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> {
                    String errorMsg = "Failed to analyze report:\n" + e.getMessage();
                    
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout") ||
                        e.getMessage().contains("Cannot connect")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Check: http://localhost:5000/api/analyze-report in browser\n" +
                                   "3. Verify Python backend server started successfully (python app.py)";
                    }
                    
                    showError("Error", errorMsg);
                    analyzeButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Unexpected Error", 
                        "An unexpected error occurred:\n" + e.getMessage() + 
                        "\n\nPlease try again or restart the application.");
                    analyzeButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void uploadAndScanImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Blood Test Report Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        Stage stage = (Stage) getScene().getWindow();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile == null) {
            return; // User cancelled
        }
        
        // Show loading indicator
        loadingIndicator.setVisible(true);
        analyzeButton.setDisable(true);
        
        NotificationHelper.showInfoNotification(
            (StackPane) getScene().getRoot(),
            "Processing image... Please wait."
        );
        
        new Thread(() -> {
            try {
                // Read file and encode to base64
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                
                // Upload and process image
                Map<String, Object> result = apiClient.uploadReportImage(base64Image);
                
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                    
                    Boolean success = (Boolean) result.get("success");
                    if (success != null && success) {
                        // Extract values from OCR result
                        @SuppressWarnings("unchecked")
                        Map<String, Object> extractedValues = (Map<String, Object>) result.get("extractedValues");
                        
                        if (extractedValues != null && !extractedValues.isEmpty()) {
                            // Populate input fields with extracted values
                            int populatedCount = 0;
                            for (Map.Entry<String, Object> entry : extractedValues.entrySet()) {
                                String paramKey = entry.getKey();
                                Object value = entry.getValue();
                                
                                TextField field = inputFields.get(paramKey);
                                if (field != null && value != null) {
                                    field.setText(String.valueOf(value));
                                    populatedCount++;
                                }
                            }
                            
                            NotificationHelper.showSuccessNotification(
                                (StackPane) getScene().getRoot(),
                                "Successfully extracted " + populatedCount + " parameter(s) from image!"
                            );
                            
                            // Optionally auto-analyze
                            if (populatedCount > 0) {
                                Platform.runLater(() -> analyzeReport());
                            }
                        } else {
                            NotificationHelper.showInfoNotification(
                                (StackPane) getScene().getRoot(),
                                "Image processed but no values could be extracted. Please enter values manually."
                            );
                        }
                    } else {
                        String errorMsg = (String) result.get("message");
                        if (errorMsg == null) {
                            errorMsg = (String) result.get("error");
                        }
                        showError("Image Processing Failed", 
                            "Failed to process image:\n" + errorMsg + 
                            "\n\nPlease ensure:\n" +
                            "1. Image is clear and well-lit\n" +
                            "2. Text is readable\n" +
                            "3. Report is in English\n\n" +
                            "You can still enter values manually.");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                    
                    String errorMsg = "Failed to upload image:\n" + e.getMessage();
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Verify Tesseract OCR is installed";
                    }
                    
                    showError("Upload Error", errorMsg);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                    showError("Error", 
                        "An error occurred while processing the image:\n" + e.getMessage() + 
                        "\n\nYou can still enter values manually.");
                });
            }
        }).start();
    }

    private void displayAnalysis(ReportAnalysis analysis) {
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));

        // Overall Status - Colorful gradient
        VBox statusBox = new VBox(10);
        String statusGradient = getStatusGradient(analysis.getOverallStatus());
        statusBox.setStyle("-fx-background-color: " + statusGradient + "; -fx-padding: 25; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);");
        HBox statusHeader = new HBox(15);
        statusHeader.setAlignment(Pos.CENTER_LEFT);
        Label statusTitle = new Label("📊 Analysis Results");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        statusTitle.setTextFill(Color.rgb(15, 118, 110));
        Label statusBadge = new Label(analysis.getOverallStatus().replace("-", " ").toUpperCase());
        statusBadge.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-padding: 8 18; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.3), 4, 0, 0, 2);");
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 13));
        statusHeader.getChildren().addAll(statusTitle, statusBadge);

        if (!analysis.getFlaggedParameters().isEmpty()) {
            VBox flaggedBox = new VBox(3);
            flaggedBox.setAlignment(Pos.CENTER_RIGHT);
            Label flaggedLabel = new Label("Flagged Parameters");
            flaggedLabel.setFont(Font.font("System", 11));
            flaggedLabel.setTextFill(Color.GRAY);
            Label flaggedCount = new Label(String.valueOf(analysis.getFlaggedParameters().size()));
            flaggedCount.setFont(Font.font("System", FontWeight.BOLD, 28));
            flaggedCount.setTextFill(Color.rgb(239, 68, 68));
            flaggedBox.getChildren().addAll(flaggedLabel, flaggedCount);
            statusHeader.getChildren().add(flaggedBox);
        }

        Label summaryLabel = new Label(analysis.getSummary());
        summaryLabel.setWrapText(true);
        summaryLabel.setFont(Font.font("System", 12));

        statusBox.getChildren().addAll(statusHeader, summaryLabel);

        // Parameters
        VBox parametersBox = new VBox(10);
        parametersBox.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-padding: 15; -fx-background-radius: 10;");
        Label paramTitle = new Label("Parameter Details");
        paramTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        parametersBox.getChildren().add(paramTitle);

        for (BloodParameter param : analysis.getParameters()) {
            VBox paramBox = new VBox(8);
            String statusColor = getStatusColor(param.getStatus());
            paramBox.setStyle("-fx-background-color: " + statusColor + "; -fx-border-color: " + getBorderColor(param.getStatus()) + "; -fx-border-width: 2; -fx-padding: 15; -fx-background-radius: 5;");

            HBox paramHeader = new HBox(10);
            paramHeader.setAlignment(Pos.CENTER_LEFT);
            Label paramName = new Label(param.getName());
            paramName.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label paramStatusBadge = new Label(param.getStatus().toUpperCase());
            paramStatusBadge.setStyle("-fx-background-color: " + getBadgeColor(param.getStatus()) + "; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 10;");
            paramStatusBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
            paramHeader.getChildren().addAll(paramName, paramStatusBadge);

            Label rangeLabel = new Label("Normal range: " + param.getNormalMin() + " - " + param.getNormalMax() + " " + param.getUnit());
            rangeLabel.setFont(Font.font("System", 11));
            rangeLabel.setTextFill(Color.GRAY);

            HBox valueBox = new HBox(10);
            valueBox.setAlignment(Pos.CENTER_RIGHT);
            Label valueLabel = new Label(String.valueOf(param.getValue()));
            valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            Label unitLabel = new Label(param.getUnit());
            unitLabel.setFont(Font.font("System", 11));
            unitLabel.setTextFill(Color.GRAY);
            valueBox.getChildren().addAll(valueLabel, unitLabel);

            paramBox.getChildren().addAll(paramHeader, rangeLabel, valueBox);
            parametersBox.getChildren().add(paramBox);
        }

        contentBox.getChildren().add(statusBox);
        contentBox.getChildren().add(parametersBox);

        // Recommendations
        if (!analysis.getRecommendations().isEmpty()) {
            VBox recommendationsBox = new VBox(10);
            recommendationsBox.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-padding: 15; -fx-background-radius: 10;");
            Label recTitle = new Label("Recommendations");
            recTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            recommendationsBox.getChildren().add(recTitle);

            VBox recList = new VBox(10);
            for (int i = 0; i < analysis.getRecommendations().size(); i++) {
                HBox recItem = new HBox(10);
                Label numLabel = new Label(String.valueOf(i + 1));
                numLabel.setStyle("-fx-background-color: #0F766E; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 50;");
                numLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                Label recText = new Label(analysis.getRecommendations().get(i));
                recText.setWrapText(true);
                recText.setFont(Font.font("System", 11));
                recItem.getChildren().addAll(numLabel, recText);
                recList.getChildren().add(recItem);
            }
            recommendationsBox.getChildren().add(recList);
            contentBox.getChildren().add(recommendationsBox);
        }

        resultsPane.setContent(contentBox);
        currentAnalysis = analysis;
        exportButton.setDisable(false);
        copyButton.setDisable(false);
        printButton.setDisable(false);
    }

    private void exportReport() {
        if (currentAnalysis == null) {
            showError("No Report", "No analysis report to export.");
            return;
        }

        String report = ReportFormatter.formatBloodReportAnalysis(currentAnalysis);
        FileExporter.exportToFile(
            report,
            "blood_report_analysis",
            "Export Blood Report Analysis",
            (Stage) getScene().getWindow()
        );
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Report exported successfully!"
        );
    }

    private void copyReport() {
        if (currentAnalysis == null) {
            showError("No Report", "No analysis report to copy.");
            return;
        }

        String report = ReportFormatter.formatBloodReportAnalysis(currentAnalysis);
        FileExporter.copyToClipboard(report);
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Report copied to clipboard!"
        );
    }

    private void printReport() {
        if (currentAnalysis == null) {
            showError("No Report", "No analysis report to print.");
            return;
        }

        String report = ReportFormatter.formatBloodReportAnalysis(currentAnalysis);
        
        // Create a simple print dialog
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(getScene().getWindow())) {
            javafx.scene.text.Text text = new javafx.scene.text.Text(report);
            text.setFont(javafx.scene.text.Font.font("Courier", 10));
            text.setWrappingWidth(500);
            
            if (job.printPage(text)) {
                job.endJob();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Print Successful");
                alert.setHeaderText(null);
                alert.setContentText("Report sent to printer successfully!");
                alert.show();
            } else {
                showError("Print Failed", "Failed to print the report.");
            }
        }
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "normal": return "#F0FDF4";
            case "low":
            case "high": return "#FEF2F2";
            default: return "#F9FAFB";
        }
    }

    private String getBorderColor(String status) {
        switch (status.toLowerCase()) {
            case "normal": return "#22C55E";
            case "low":
            case "high": return "#EF4444";
            default: return "#E5E7EB";
        }
    }

    private String getBadgeColor(String status) {
        switch (status.toLowerCase()) {
            case "normal": return "#22C55E";
            case "low":
            case "high": return "#EF4444";
            default: return "#6B7280";
        }
    }
    
    private String getStatusGradient(String status) {
        switch (status.toLowerCase()) {
            case "normal": return "linear-gradient(to right, #F0FDF4, #D1FAE5)";
            case "requires-attention": return "linear-gradient(to right, #FFFBEB, #FEF3C7)";
            case "abnormal": return "linear-gradient(to right, #FEF2F2, #FEE2E2)";
            default: return "linear-gradient(to right, #E0F2FE, #F0FDFA)";
        }
    }

    private void showError(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
}


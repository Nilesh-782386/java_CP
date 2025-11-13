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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

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
    private VBox ocrSummaryBox;
    private TextArea ocrTextArea;
    private TitledPane ocrDetailsPane;
    private Map<String, Object> lastExtractedValues = new HashMap<>();
    private String lastOcrText;
    private GridPane ocrValuesGrid;

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

        // No header - ModulePageWrapper handles it with back button
        HBox mainContent = new HBox(25);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #F0F9FF;");

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        mainContent.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        setCenter(mainContent);
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(12);
        leftPanel.getStyleClass().add("panel");
        leftPanel.setMinWidth(400);
        leftPanel.setMaxWidth(480);
        leftPanel.setPrefWidth(450);
        VBox.setVgrow(leftPanel, Priority.SOMETIMES);

        Label panelTitle = new Label("Blood Test Parameters");
        panelTitle.getStyleClass().add("panel-title");
        
        // Add image upload option
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E5E7EB;");
        
        Label uploadLabel = new Label("📷 Quick Input Options");
        uploadLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        uploadLabel.setTextFill(Color.web("#0F766E"));
        
        // Option 1: Paste Text (No OCR needed)
        Button pasteTextButton = new Button("📋 Paste Report Text");
        pasteTextButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #10B981, #34D399); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 15; " +
            "-fx-cursor: hand;"
        );
        pasteTextButton.setPrefWidth(Double.MAX_VALUE);
        pasteTextButton.setTooltip(new Tooltip("Paste text from your report to automatically extract values (No OCR needed)"));
        pasteTextButton.setOnAction(e -> showPasteTextDialog());
        
        // Option 2: Upload Image (OCR - requires Tesseract)
        Button uploadImageButton = new Button("📤 Upload & Scan Image (OCR)");
        uploadImageButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #8B5CF6, #A78BFA); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 15; " +
            "-fx-cursor: hand;"
        );
        uploadImageButton.setPrefWidth(Double.MAX_VALUE);
        uploadImageButton.setTooltip(new Tooltip("Upload a photo of your report (Requires Tesseract OCR installed)"));
        uploadImageButton.setOnAction(e -> uploadAndScanImage());
        
        Label orLabel = new Label("OR");
        orLabel.setFont(Font.font("System", 11));
        orLabel.setTextFill(Color.GRAY);
        orLabel.setAlignment(Pos.CENTER);
        orLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label manualLabel = new Label("Enter Values Manually");
        manualLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        manualLabel.setTextFill(Color.GRAY);

        // Wrap inputs in ScrollPane for better fit
        VBox inputsBox = new VBox(10);
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
            inputField.setPrefHeight(32);
            inputField.setMaxHeight(32);
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
        loadingIndicator.setPrefSize(25, 25);

        // Wrap inputs in ScrollPane to prevent overflow
        ScrollPane inputsScrollPane = new ScrollPane(inputsBox);
        inputsScrollPane.setFitToWidth(true);
        inputsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        inputsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        inputsScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        inputsScrollPane.setPrefHeight(300);
        inputsScrollPane.setMaxHeight(350);
        VBox.setVgrow(inputsScrollPane, Priority.ALWAYS);

        leftPanel.getChildren().addAll(
            panelTitle, 
            separator,
            uploadLabel,
            pasteTextButton,
            uploadImageButton,
            orLabel,
            manualLabel,
            inputsScrollPane, 
            analyzeButton, 
            loadingIndicator
        );

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(12);
        rightPanel.getStyleClass().add("panel");
        rightPanel.setMinWidth(600);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);

        // Action buttons for results
        HBox actionButtonsBox = new HBox(8);
        actionButtonsBox.setAlignment(Pos.CENTER_RIGHT);
        actionButtonsBox.setPadding(new Insets(5, 0, 5, 0));
        
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

        ocrSummaryBox = new VBox(10);
        ocrSummaryBox.setStyle(
            "-fx-background-color: #f0fdf4;" +
            "-fx-border-color: #22c55e;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 14;"
        );
        ocrSummaryBox.setVisible(false);
        ocrSummaryBox.setManaged(false);

        Label ocrTitle = new Label("📷 OCR Extraction Summary");
        ocrTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        ocrTitle.setTextFill(Color.web("#166534"));

        HBox ocrActions = new HBox(8);
        ocrActions.setAlignment(Pos.CENTER_LEFT);

        Button copyOcrButton = new Button("Copy Summary");
        copyOcrButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold;");
        copyOcrButton.setOnAction(e -> copyOcrSummaryToClipboard());

        Button clearOcrButton = new Button("Clear");
        clearOcrButton.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold;");
        clearOcrButton.setOnAction(e -> clearOcrSummary());

        Button analyzeOcrButton = new Button("Analyze Again");
        analyzeOcrButton.setStyle("-fx-background-color: #0f766e; -fx-text-fill: white; -fx-font-weight: bold;");
        analyzeOcrButton.setOnAction(e -> analyzeReport());

        ocrActions.getChildren().addAll(copyOcrButton, clearOcrButton, analyzeOcrButton);

        ocrTextArea = new TextArea();
        ocrTextArea.setEditable(false);
        ocrTextArea.setWrapText(true);
        ocrTextArea.setPrefRowCount(6);
        ocrTextArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

        ocrDetailsPane = new TitledPane("Raw OCR Text (preview)", ocrTextArea);
        ocrDetailsPane.setExpanded(false);

        ocrValuesGrid = new GridPane();
        ocrValuesGrid.setHgap(12);
        ocrValuesGrid.setVgap(6);

        ocrSummaryBox.getChildren().addAll(ocrTitle, ocrActions, ocrValuesGrid, ocrDetailsPane);

        resultsPane = new ScrollPane();
        resultsPane.setFitToWidth(true);
        resultsPane.setFitToHeight(true);
        resultsPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        resultsPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        resultsPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
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

        rightPanel.getChildren().addAll(actionButtonsBox, ocrSummaryBox, resultsPane);

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
                        
                        renderOcrSummary(extractedValues, (String) result.get("ocrText"));

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
                            
                            // Optionally auto-analyze when required values present
                            if (hasAllRequiredValues(extractedValues)) {
                                Platform.runLater(this::analyzeReport);
                            } else if (populatedCount > 0) {
                                NotificationHelper.showInfoNotification(
                                    (StackPane) getScene().getRoot(),
                                    "Some required parameters are still missing. Please review highlighted fields before analysis."
                                );
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
                        if (errorMsg != null && errorMsg.toLowerCase().contains("tesseract")) {
                            errorMsg += "\n\nTesseract OCR engine was not found. Please install Tesseract and restart the backend.";
                        }
                        clearOcrSummary();
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
                    
                    clearOcrSummary();
                    showError("Upload Error", errorMsg);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                    clearOcrSummary();
                    showError("Error", 
                        "An error occurred while processing the image:\n" + e.getMessage() + 
                        "\n\nYou can still enter values manually.");
                });
            }
        }).start();
    }

    private void showPasteTextDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Paste Report Text");
        dialog.setHeaderText("Paste the text from your blood test report below");
        
        TextArea textArea = new TextArea();
        textArea.setPromptText("Paste your report text here...\nExample:\nHemoglobin: 14.5 g/dL\nWBC: 7000 cells/µL\nPlatelets: 250000 cells/µL");
        textArea.setWrapText(true);
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(40);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.getChildren().addAll(
            new Label("Copy and paste the text from your report:"),
            textArea
        );
        
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return textArea.getText();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(text -> {
            if (text != null && !text.trim().isEmpty()) {
                processPastedText(text);
            }
        });
    }
    
    private void processPastedText(String text) {
        loadingIndicator.setVisible(true);
        analyzeButton.setDisable(true);
        
        NotificationHelper.showInfoNotification(
            (StackPane) getScene().getRoot(),
            "Extracting values from text... Please wait."
        );
        
        new Thread(() -> {
            try {
                // Send text to backend for parsing
                Map<String, Object> result = apiClient.parseReportText(text);
                
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                    
                    Boolean success = (Boolean) result.get("success");
                    if (success != null && success) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> extractedValues = (Map<String, Object>) result.get("extractedValues");
                        
                        renderOcrSummary(extractedValues, text);

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
                                "Successfully extracted " + populatedCount + " parameter(s) from text!"
                            );
                            
                            // Optionally auto-analyze when required values present
                            if (hasAllRequiredValues(extractedValues)) {
                                Platform.runLater(this::analyzeReport);
                            } else if (populatedCount > 0) {
                                NotificationHelper.showInfoNotification(
                                    (StackPane) getScene().getRoot(),
                                    "Some required parameters are still missing. Please review highlighted fields before analysis."
                                );
                            }
                        } else {
                            NotificationHelper.showInfoNotification(
                                (StackPane) getScene().getRoot(),
                                "No values could be extracted. Please enter values manually."
                            );
                        }
                    } else {
                        String errorMsg = (String) result.get("message");
                        if (errorMsg == null) {
                            errorMsg = (String) result.get("error");
                        }
                        clearOcrSummary();
                        showError("Text Processing Failed", 
                            "Failed to extract values from text:\n" + errorMsg + 
                            "\n\nPlease ensure the text contains parameter names and values.\n" +
                            "You can still enter values manually.");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                    
                    String errorMsg = "Failed to process text:\n" + e.getMessage();
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Check: http://localhost:5000/api/parse-report-text in browser";
                    }
                    
                    clearOcrSummary();
                    showError("Processing Error", errorMsg);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                    clearOcrSummary();
                    showError("Error", 
                        "An error occurred while processing the text:\n" + e.getMessage() + 
                        "\n\nYou can still enter values manually.");
                });
            }
        }).start();
    }
    
    private void renderOcrSummary(Map<String, Object> extractedValues, String ocrText) {
        if (ocrSummaryBox == null || ocrValuesGrid == null) {
            return;
        }

        lastExtractedValues = extractedValues != null ? new HashMap<>(extractedValues) : new HashMap<>();
        lastOcrText = ocrText;

        ocrValuesGrid.getChildren().clear();

        if (lastExtractedValues.isEmpty()) {
            Label empty = new Label("No numeric values could be detected. Please verify the report or enter values manually.");
            empty.setStyle("-fx-text-fill: #166534; -fx-font-size: 13px;");
            ocrValuesGrid.add(empty, 0, 0);
        } else {
            int row = 0;
            for (String[] param : BLOOD_PARAMETERS) {
                String key = param[0];
                if (lastExtractedValues.containsKey(key)) {
                    String label = param[1];
                    String unit = param[2];
                    Object valueObj = lastExtractedValues.get(key);
                    String displayValue = String.valueOf(valueObj);
                    double numericValue;
                    boolean numeric = true;
                    try {
                        numericValue = Double.parseDouble(displayValue);
                    } catch (NumberFormatException ex) {
                        numeric = false;
                        numericValue = 0;
                    }

                    Label nameLabel = new Label(label + ":");
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #166534;");

                    Label valueLabel = new Label(
                        numeric ? String.format("%.2f %s", numericValue, unit) : displayValue + " " + unit
                    );
                    valueLabel.setStyle("-fx-text-fill: #0b4a32;");

                    ocrValuesGrid.add(nameLabel, 0, row);
                    ocrValuesGrid.add(valueLabel, 1, row);
                    row++;
                }
            }
        }

        if (ocrText != null && !ocrText.isBlank()) {
            ocrTextArea.setText(ocrText.trim());
            ocrDetailsPane.setVisible(true);
            ocrDetailsPane.setManaged(true);
        } else {
            ocrTextArea.clear();
            ocrDetailsPane.setVisible(false);
            ocrDetailsPane.setManaged(false);
        }

        ocrSummaryBox.setVisible(true);
        ocrSummaryBox.setManaged(true);
    }

    private void clearOcrSummary() {
        lastExtractedValues.clear();
        lastOcrText = null;
        if (ocrValuesGrid != null) {
            ocrValuesGrid.getChildren().clear();
        }
        if (ocrTextArea != null) {
            ocrTextArea.clear();
        }
        if (ocrSummaryBox != null) {
            ocrSummaryBox.setVisible(false);
            ocrSummaryBox.setManaged(false);
        }
    }

    private void copyOcrSummaryToClipboard() {
        if ((lastExtractedValues == null || lastExtractedValues.isEmpty()) && (lastOcrText == null || lastOcrText.isBlank())) {
            StackPane root = getScene() != null && getScene().getRoot() instanceof StackPane
                ? (StackPane) getScene().getRoot()
                : null;
            if (root != null) {
                NotificationHelper.showInfoNotification(root, "No OCR summary available to copy.");
            }
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("OCR Extraction Summary\n");
        sb.append("======================\n");
        if (lastExtractedValues != null && !lastExtractedValues.isEmpty()) {
            for (String[] param : BLOOD_PARAMETERS) {
                String key = param[0];
                if (lastExtractedValues.containsKey(key)) {
                    sb.append(String.format("%s: %s %s\n", param[1], lastExtractedValues.get(key), param[2]));
                }
            }
            // Include any additional keys not in default list
            for (String key : lastExtractedValues.keySet()) {
                boolean known = false;
                for (String[] param : BLOOD_PARAMETERS) {
                    if (param[0].equals(key)) {
                        known = true;
                        break;
                    }
                }
                if (!known) {
                    sb.append(String.format("%s: %s\n", key, lastExtractedValues.get(key)));
                }
            }
        } else {
            sb.append("No values extracted.\n");
        }

        if (lastOcrText != null && !lastOcrText.isBlank()) {
            sb.append("\nRaw OCR Text Preview:\n");
            sb.append(lastOcrText.trim());
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);

        StackPane root = getScene() != null && getScene().getRoot() instanceof StackPane
            ? (StackPane) getScene().getRoot()
            : null;
        if (root != null) {
            NotificationHelper.showSuccessNotification(root, "OCR summary copied to clipboard.");
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Copied");
            alert.setHeaderText(null);
            alert.setContentText("OCR summary copied to clipboard.");
            alert.show();
        }
    }

    private String getParameterLabel(String key) {
        for (String[] param : BLOOD_PARAMETERS) {
            if (param[0].equals(key)) {
                return param[1];
            }
        }
        return key;
    }

    private boolean hasAllRequiredValues(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        for (String[] param : BLOOD_PARAMETERS) {
            String key = param[0];
            boolean optional = Boolean.parseBoolean(param[4]);
            if (!optional) {
                Object val = values.get(key);
                if (val == null) {
                    return false;
                }
                try {
                    double numeric = Double.parseDouble(String.valueOf(val));
                    if (numeric <= 0) {
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
        }
        return true;
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


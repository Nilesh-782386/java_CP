package com.smartheal.views;

import com.smartheal.api.ApiClient;
import com.smartheal.dao.HistoryDAO;
import com.smartheal.models.RiskAssessment;
import com.smartheal.models.Recommendation;
import com.smartheal.models.Symptom;
import com.smartheal.utils.NotificationHelper;
import com.smartheal.utils.FileExporter;
import com.smartheal.utils.ReportFormatter;
import com.smartheal.utils.JSONExporter;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RiskAssessmentView extends BorderPane {
    private final ApiClient apiClient;
    private final HistoryDAO historyDAO;
    private final ObjectMapper objectMapper;
    private Integer currentUserId = null;
    
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
    
    // Form fields
    private TextField ageField;
    private TextField weightField;
    private TextField heightField;
    private CheckBox smokingCheckBox;
    private ComboBox<String> exerciseCombo;
    private ComboBox<String> alcoholCombo;
    private TextField sleepHoursField;
    private ComboBox<String> stressLevelCombo;
    private ComboBox<String> dietQualityCombo;
    private ListView<String> symptomsList;
    private ObservableList<String> selectedSymptoms;
    private ListView<String> familyHistoryList;
    private ObservableList<String> familyHistory;
    private TextField newSymptomField;
    private TextField newFamilyHistoryField;
    private Button addSymptomButton;
    private Button addFamilyHistoryButton;
    private Button assessButton;
    private ProgressIndicator loadingIndicator;
    
    // Results display
    private ScrollPane resultsPane;
    private VBox resultsContainer;
    private RiskAssessment currentAssessment;
    private Button exportButton;
    private Button exportJsonButton;
    private Button copyButton;
    
    // Store input data for export
    private int lastAge;
    private double lastWeight;
    private double lastHeight;
    private List<String> lastSymptoms;
    private List<String> lastFamilyHistory;
    
    // Available symptoms from API
    private List<Symptom> allSymptoms;

    public RiskAssessmentView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.historyDAO = new HistoryDAO();
        this.objectMapper = new ObjectMapper();
        this.selectedSymptoms = FXCollections.observableArrayList();
        this.familyHistory = FXCollections.observableArrayList();
        this.allSymptoms = new ArrayList<>();

        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(15));
        mainContent.setStyle("-fx-background-color: #F0F9FF;");

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        mainContent.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.SOMETIMES);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        // Make panels responsive
        leftPanel.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.45));
        rightPanel.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.55));

        setCenter(mainContent);
        
        // Load symptoms asynchronously
        Platform.runLater(() -> loadSymptoms());
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(0);
        leftPanel.getStyleClass().add("panel");
        leftPanel.setMinWidth(450);
        leftPanel.setMaxWidth(550);
        VBox.setVgrow(leftPanel, Priority.ALWAYS);

        // Header - always visible
        Label panelTitle = new Label("Health Information");
        panelTitle.getStyleClass().add("panel-title");
        panelTitle.setStyle("-fx-padding: 15 20; -fx-background-color: white;");

        // Basic Info Section
        VBox basicInfoBox = new VBox(10);
        Label basicInfoLabel = new Label("Basic Information");
        basicInfoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        basicInfoLabel.setTextFill(Color.web("#0F766E"));

        HBox ageBox = new HBox(10);
        ageBox.setAlignment(Pos.CENTER_LEFT);
        Label ageLabel = new Label("Age (years):");
        ageLabel.setMinWidth(120);
        ageField = new TextField();
        ageField.setPromptText("e.g., 45");
        ageField.setPrefWidth(150);
        HBox.setHgrow(ageField, Priority.ALWAYS);
        ageBox.getChildren().addAll(ageLabel, ageField);

        HBox weightBox = new HBox(10);
        weightBox.setAlignment(Pos.CENTER_LEFT);
        Label weightLabel = new Label("Weight (kg):");
        weightLabel.setMinWidth(120);
        weightField = new TextField();
        weightField.setPromptText("e.g., 75");
        weightField.setPrefWidth(150);
        HBox.setHgrow(weightField, Priority.ALWAYS);
        weightBox.getChildren().addAll(weightLabel, weightField);

        HBox heightBox = new HBox(10);
        heightBox.setAlignment(Pos.CENTER_LEFT);
        Label heightLabel = new Label("Height (cm):");
        heightLabel.setMinWidth(120);
        heightField = new TextField();
        heightField.setPromptText("e.g., 170");
        heightField.setPrefWidth(150);
        HBox.setHgrow(heightField, Priority.ALWAYS);
        heightBox.getChildren().addAll(heightLabel, heightField);

        basicInfoBox.getChildren().addAll(basicInfoLabel, ageBox, weightBox, heightBox);

        // Lifestyle Section
        VBox lifestyleBox = new VBox(10);
        Label lifestyleLabel = new Label("Lifestyle Factors");
        lifestyleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        lifestyleLabel.setTextFill(Color.web("#0F766E"));

        smokingCheckBox = new CheckBox("Smoking");
        smokingCheckBox.setFont(Font.font("System", 13));

        HBox exerciseBox = new HBox(10);
        exerciseBox.setAlignment(Pos.CENTER_LEFT);
        Label exerciseLabel = new Label("Exercise Level:");
        exerciseLabel.setMinWidth(120);
        exerciseCombo = new ComboBox<>();
        exerciseCombo.getItems().addAll("None", "Moderate", "High");
        exerciseCombo.setValue("Moderate");
        exerciseCombo.setPrefWidth(150);
        HBox.setHgrow(exerciseCombo, Priority.ALWAYS);
        exerciseBox.getChildren().addAll(exerciseLabel, exerciseCombo);

        HBox alcoholBox = new HBox(10);
        alcoholBox.setAlignment(Pos.CENTER_LEFT);
        Label alcoholLabel = new Label("Alcohol:");
        alcoholLabel.setMinWidth(120);
        alcoholCombo = new ComboBox<>();
        alcoholCombo.getItems().addAll("None", "Moderate", "Heavy");
        alcoholCombo.setValue("None");
        alcoholCombo.setPrefWidth(150);
        HBox.setHgrow(alcoholCombo, Priority.ALWAYS);
        alcoholBox.getChildren().addAll(alcoholLabel, alcoholCombo);

        HBox sleepBox = new HBox(10);
        sleepBox.setAlignment(Pos.CENTER_LEFT);
        Label sleepLabel = new Label("Sleep (hours/night):");
        sleepLabel.setMinWidth(120);
        sleepHoursField = new TextField();
        sleepHoursField.setPromptText("e.g., 7.5");
        sleepHoursField.setPrefWidth(150);
        HBox.setHgrow(sleepHoursField, Priority.ALWAYS);
        sleepBox.getChildren().addAll(sleepLabel, sleepHoursField);

        HBox stressBox = new HBox(10);
        stressBox.setAlignment(Pos.CENTER_LEFT);
        Label stressLabel = new Label("Stress Level:");
        stressLabel.setMinWidth(120);
        stressLevelCombo = new ComboBox<>();
        stressLevelCombo.getItems().addAll("Low", "Moderate", "High");
        stressLevelCombo.setValue("Moderate");
        stressLevelCombo.setPrefWidth(150);
        HBox.setHgrow(stressLevelCombo, Priority.ALWAYS);
        stressBox.getChildren().addAll(stressLabel, stressLevelCombo);

        HBox dietBox = new HBox(10);
        dietBox.setAlignment(Pos.CENTER_LEFT);
        Label dietLabel = new Label("Diet Quality:");
        dietLabel.setMinWidth(120);
        dietQualityCombo = new ComboBox<>();
        dietQualityCombo.getItems().addAll("Poor", "Moderate", "Good");
        dietQualityCombo.setValue("Moderate");
        dietQualityCombo.setPrefWidth(150);
        HBox.setHgrow(dietQualityCombo, Priority.ALWAYS);
        dietBox.getChildren().addAll(dietLabel, dietQualityCombo);

        lifestyleBox.getChildren().addAll(lifestyleLabel, smokingCheckBox, exerciseBox, alcoholBox, sleepBox, stressBox, dietBox);

        // Symptoms Section
        VBox symptomsBox = new VBox(10);
        Label symptomsLabel = new Label("Current Symptoms (Optional)");
        symptomsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        symptomsLabel.setTextFill(Color.web("#0F766E"));

        HBox addSymptomBox = new HBox(5);
        newSymptomField = new TextField();
        newSymptomField.setPromptText("Enter symptom");
        HBox.setHgrow(newSymptomField, Priority.ALWAYS);
        addSymptomButton = new Button("Add");
        addSymptomButton.setStyle("-fx-background-color: #14B8A6; -fx-text-fill: white; -fx-font-weight: bold;");
        addSymptomButton.setOnAction(e -> {
            String symptom = newSymptomField.getText().trim();
            if (!symptom.isEmpty() && !selectedSymptoms.contains(symptom)) {
                selectedSymptoms.add(symptom);
                newSymptomField.clear();
            }
        });
        addSymptomBox.getChildren().addAll(newSymptomField, addSymptomButton);

        symptomsList = new ListView<>(selectedSymptoms);
        symptomsList.setPrefHeight(100);
        symptomsList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    Button removeBtn = new Button("✕");
                    removeBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 10px;");
                    removeBtn.setOnAction(e -> selectedSymptoms.remove(item));
                    HBox box = new HBox(5, new javafx.scene.control.Label(item), removeBtn);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        symptomsBox.getChildren().addAll(symptomsLabel, addSymptomBox, symptomsList);

        // Family History Section
        VBox familyHistoryBox = new VBox(10);
        Label familyHistoryLabel = new Label("Family History (Optional)");
        familyHistoryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        familyHistoryLabel.setTextFill(Color.web("#0F766E"));

        HBox addFamilyBox = new HBox(5);
        newFamilyHistoryField = new TextField();
        newFamilyHistoryField.setPromptText("e.g., diabetes, heart disease");
        HBox.setHgrow(newFamilyHistoryField, Priority.ALWAYS);
        addFamilyHistoryButton = new Button("Add");
        addFamilyHistoryButton.setStyle("-fx-background-color: #14B8A6; -fx-text-fill: white; -fx-font-weight: bold;");
        addFamilyHistoryButton.setOnAction(e -> {
            String history = newFamilyHistoryField.getText().trim();
            if (!history.isEmpty() && !familyHistory.contains(history)) {
                familyHistory.add(history);
                newFamilyHistoryField.clear();
            }
        });
        addFamilyBox.getChildren().addAll(newFamilyHistoryField, addFamilyHistoryButton);

        familyHistoryList = new ListView<>(familyHistory);
        familyHistoryList.setPrefHeight(100);
        familyHistoryList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    Button removeBtn = new Button("✕");
                    removeBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 10px;");
                    removeBtn.setOnAction(e -> familyHistory.remove(item));
                    HBox box = new HBox(5, new javafx.scene.control.Label(item), removeBtn);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        familyHistoryBox.getChildren().addAll(familyHistoryLabel, addFamilyBox, familyHistoryList);

        // Assess Button
        assessButton = new Button("🔍 Assess Health Risks");
        assessButton.getStyleClass().addAll("button", "button-primary");
        assessButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        assessButton.setPrefHeight(50);
        assessButton.setPrefWidth(Double.MAX_VALUE);
        assessButton.setCursor(javafx.scene.Cursor.HAND);
        assessButton.setOnAction(e -> assessRisks());

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);

        // Create scrollable content area
        VBox scrollableContent = new VBox(15);
        scrollableContent.setStyle("-fx-background-color: white; -fx-padding: 20;");
        scrollableContent.getChildren().addAll(basicInfoBox, lifestyleBox, symptomsBox, familyHistoryBox);
        
        ScrollPane scrollPane = new ScrollPane(scrollableContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Footer with button - always visible at bottom
        VBox footerBox = new VBox(10);
        footerBox.setStyle("-fx-background-color: white; -fx-padding: 15 20;");
        footerBox.getChildren().addAll(assessButton, loadingIndicator);
        
        leftPanel.getChildren().addAll(panelTitle, scrollPane, footerBox);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(0);
        rightPanel.getStyleClass().add("panel");
        rightPanel.setMinWidth(500);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setFillWidth(true);

        // Header - always visible
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-padding: 15 20; -fx-background-color: white;");
        Label panelTitle = new Label("Risk Assessment Results");
        panelTitle.getStyleClass().add("panel-title");

        exportButton = new Button("💾 Export TXT");
        exportButton.setStyle("-fx-background-color: linear-gradient(to bottom, #3B82F6, #60A5FA); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12;");
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> exportResults());

        exportJsonButton = new Button("📄 Export JSON");
        exportJsonButton.setStyle("-fx-background-color: linear-gradient(to bottom, #10B981, #34D399); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12;");
        exportJsonButton.setDisable(true);
        exportJsonButton.setOnAction(e -> exportResultsJSON());

        copyButton = new Button("📋 Copy");
        copyButton.setStyle("-fx-background-color: linear-gradient(to bottom, #6366F1, #818CF8); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12;");
        copyButton.setDisable(true);
        copyButton.setOnAction(e -> copyResults());

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(exportButton, exportJsonButton, copyButton);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        headerBox.getChildren().addAll(panelTitle, buttonBox);

        resultsContainer = new VBox(15);
        resultsContainer.setStyle("-fx-background-color: #F0F9FF; -fx-padding: 20;");
        resultsContainer.setAlignment(Pos.TOP_CENTER);

        resultsPane = new ScrollPane(resultsContainer);
        resultsPane.setFitToWidth(true);
        resultsPane.setFitToHeight(true);
        resultsPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        resultsPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        resultsPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(resultsPane, Priority.ALWAYS);

        Label placeholderLabel = new Label("Fill in your health information and click 'Assess Health Risks' to see your personalized risk assessment.");
        placeholderLabel.setWrapText(true);
        placeholderLabel.setFont(Font.font("System", 14));
        placeholderLabel.setTextFill(Color.GRAY);
        placeholderLabel.setAlignment(Pos.CENTER);
        resultsContainer.getChildren().add(placeholderLabel);

        rightPanel.getChildren().addAll(headerBox, resultsPane);

        return rightPanel;
    }

    private void loadSymptoms() {
        new Thread(() -> {
            try {
                allSymptoms = apiClient.getSymptoms();
            } catch (IOException e) {
                System.err.println("Failed to load symptoms: " + e.getMessage());
            }
        }).start();
    }

    private void assessRisks() {
        // Validate inputs
        String ageText = ageField.getText().trim();
        String weightText = weightField.getText().trim();
        String heightText = heightField.getText().trim();

        if (ageText.isEmpty() || weightText.isEmpty() || heightText.isEmpty()) {
            showError("Missing Information", "Please fill in Age, Weight, and Height fields.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            double weight = Double.parseDouble(weightText);
            double height = Double.parseDouble(heightText);

            if (age < 18 || age > 100) {
                showError("Invalid Age", "Age must be between 18 and 100 years.");
                return;
            }
            if (weight < 30 || weight > 200) {
                showError("Invalid Weight", "Weight must be between 30 and 200 kg.");
                return;
            }
            if (height < 100 || height > 250) {
                showError("Invalid Height", "Height must be between 100 and 250 cm.");
                return;
            }

            assessButton.setDisable(true);
            loadingIndicator.setVisible(true);
            resultsContainer.getChildren().clear();

            // Get exercise level (0 = none, 1 = moderate, 2 = high)
            final int exerciseLevel = exerciseCombo.getSelectionModel().getSelectedIndex();
            final int alcoholLevel = alcoholCombo.getSelectionModel().getSelectedIndex();
            
            // Get optional enhanced features - make final for lambda
            final Double sleepHours;
            String sleepText = sleepHoursField.getText().trim();
            if (!sleepText.isEmpty()) {
                try {
                    double parsedSleep = Double.parseDouble(sleepText);
                    if (parsedSleep < 0 || parsedSleep > 24) {
                        showError("Invalid Sleep Hours", "Sleep hours must be between 0 and 24.");
                        return;
                    }
                    sleepHours = parsedSleep;
                } catch (NumberFormatException e) {
                    showError("Invalid Sleep Hours", "Please enter a valid number for sleep hours.");
                    return;
                }
            } else {
                sleepHours = null;
            }
            
            final int stressLevel = stressLevelCombo.getSelectionModel().getSelectedIndex();
            final int dietQuality = dietQualityCombo.getSelectionModel().getSelectedIndex();
            
            // Make variables final for lambda
            final int finalAge = age;
            final double finalWeight = weight;
            final double finalHeight = height;

            new Thread(() -> {
                try {
                    // Call API with enhanced features
                    RiskAssessment assessment = apiClient.assessRisk(
                        finalAge, finalWeight, finalHeight,
                        new ArrayList<>(selectedSymptoms),
                        new ArrayList<>(familyHistory),
                        smokingCheckBox.isSelected(),
                        exerciseLevel,
                        alcoholLevel,
                        sleepHours,
                        stressLevel,
                        dietQuality
                    );

                    Platform.runLater(() -> {
                        currentAssessment = assessment;
                        // Store input data for export
                        lastAge = finalAge;
                        lastWeight = finalWeight;
                        lastHeight = finalHeight;
                        lastSymptoms = new ArrayList<>(selectedSymptoms);
                        lastFamilyHistory = new ArrayList<>(familyHistory);
                        
                        displayResults(assessment);
                        assessButton.setDisable(false);
                        loadingIndicator.setVisible(false);
                        exportButton.setDisable(false);
                        exportJsonButton.setDisable(false);
                        copyButton.setDisable(false);

                        // Save to history
                        if (currentUserId != null) {
                            try {
                                String assessmentJson = objectMapper.writeValueAsString(assessment);
                                historyDAO.saveRiskHistory(currentUserId, finalAge, finalWeight, finalHeight, assessmentJson);
                            } catch (Exception e) {
                                System.err.println("Failed to save risk history: " + e.getMessage());
                            }
                        }

                        NotificationHelper.showSuccessNotification(
                            (StackPane) getScene().getRoot(),
                            "Risk assessment completed successfully!"
                        );
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showError("Assessment Error", "Failed to assess risks:\n" + e.getMessage());
                        assessButton.setDisable(false);
                        loadingIndicator.setVisible(false);
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter valid numbers for Age, Weight, and Height.");
        }
    }

    private void displayResults(RiskAssessment assessment) {
        resultsContainer.getChildren().clear();

        // Health Score Display (if available)
        if (assessment.getHealthScore() != null) {
            VBox healthScoreBox = createHealthScoreCard(assessment.getHealthScore());
            resultsContainer.getChildren().add(healthScoreBox);
        }

        // BMI Display
        VBox bmiBox = createInfoCard("BMI", String.format("%.1f", assessment.getBmi()), 
            getBMICategory(assessment.getBmi()), "#3B82F6");
        resultsContainer.getChildren().add(bmiBox);

        // Risk Progress Bars
        VBox diabetesCard = createRiskCard("Diabetes Risk", assessment.getDiabetesRisk(), "#EF4444");
        VBox heartCard = createRiskCard("Heart Disease Risk", assessment.getHeartRisk(), "#DC2626");
        VBox hypertensionCard = createRiskCard("Hypertension Risk", assessment.getHypertensionRisk(), "#F59E0B");

        resultsContainer.getChildren().addAll(diabetesCard, heartCard, hypertensionCard);

        // Estimated Values
        if (assessment.getEstimatedValues() != null && !assessment.getEstimatedValues().isEmpty()) {
            VBox estimatedBox = new VBox(10);
            estimatedBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10;");
            Label estimatedLabel = new Label("Estimated Health Values");
            estimatedLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            estimatedBox.getChildren().add(estimatedLabel);

            assessment.getEstimatedValues().forEach((key, value) -> {
                HBox row = new HBox(10);
                Label keyLabel = new Label(formatKey(key) + ":");
                keyLabel.setMinWidth(150);
                Label valueLabel = new Label(String.format("%.1f", value) + getUnit(key));
                valueLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                row.getChildren().addAll(keyLabel, valueLabel);
                estimatedBox.getChildren().add(row);
            });

            resultsContainer.getChildren().add(estimatedBox);
        }

        // Risk Trend Display (if available)
        if (assessment.getRiskTrend() != null && !assessment.getRiskTrend().isEmpty()) {
            VBox trendBox = createRiskTrendCard(assessment.getRiskTrend());
            resultsContainer.getChildren().add(trendBox);
        }

        // Risk Explanations (if available)
        if (assessment.getRiskExplanations() != null && !assessment.getRiskExplanations().isEmpty()) {
            VBox explanationsBox = createRiskExplanationsCard(assessment.getRiskExplanations());
            resultsContainer.getChildren().add(explanationsBox);
        }

        // Feature Importance (if available)
        if (assessment.getFeatureImportance() != null && !assessment.getFeatureImportance().isEmpty()) {
            VBox importanceBox = createFeatureImportanceCard(assessment.getFeatureImportance());
            resultsContainer.getChildren().add(importanceBox);
        }

        // Risk Reduction Calculator (if available)
        if (assessment.getRiskReduction() != null && !assessment.getRiskReduction().isEmpty()) {
            VBox reductionBox = createRiskReductionCard(assessment.getRiskReduction());
            resultsContainer.getChildren().add(reductionBox);
        }

        // Action Plan (if available)
        if (assessment.getActionPlan() != null && !assessment.getActionPlan().isEmpty()) {
            VBox actionPlanBox = createActionPlanCard(assessment.getActionPlan());
            resultsContainer.getChildren().add(actionPlanBox);
        }

        // Screening Recommendations (if available)
        if (assessment.getScreeningRecommendations() != null && !assessment.getScreeningRecommendations().isEmpty()) {
            VBox screeningBox = createScreeningCard(assessment.getScreeningRecommendations());
            resultsContainer.getChildren().add(screeningBox);
        }

        // Population Comparison (if available)
        if (assessment.getPopulationComparison() != null) {
            VBox comparisonBox = createPopulationComparisonCard(assessment.getPopulationComparison());
            resultsContainer.getChildren().add(comparisonBox);
        }

        // Recommendations
        if (assessment.getRecommendations() != null && !assessment.getRecommendations().isEmpty()) {
            VBox recommendationsBox = new VBox(10);
            recommendationsBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10;");
            Label recLabel = new Label("Recommendations");
            recLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            recommendationsBox.getChildren().add(recLabel);

            for (Recommendation rec : assessment.getRecommendations()) {
                VBox recCard = new VBox(5);
                recCard.setStyle("-fx-background-color: " + getPriorityColor(rec.getPriority()) + "; -fx-padding: 10; -fx-background-radius: 8;");
                Label priorityLabel = new Label(rec.getPriority().toUpperCase() + " PRIORITY - " + rec.getCategory().toUpperCase());
                priorityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                Label messageLabel = new Label(rec.getMessage());
                messageLabel.setWrapText(true);
                messageLabel.setFont(Font.font("System", 13));
                recCard.getChildren().addAll(priorityLabel, messageLabel);
                recommendationsBox.getChildren().add(recCard);
            }

            resultsContainer.getChildren().add(recommendationsBox);
        }
    }

    private VBox createRiskCard(String title, int riskPercentage, String color) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        ProgressBar progressBar = new ProgressBar(riskPercentage / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(25);
        progressBar.setStyle("-fx-accent: " + color + ";");

        HBox percentageBox = new HBox(10);
        percentageBox.setAlignment(Pos.CENTER_LEFT);
        Label percentageLabel = new Label(riskPercentage + "%");
        percentageLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        percentageLabel.setTextFill(Color.web(color));

        Label riskLevelLabel = new Label(getRiskLevel(riskPercentage));
        riskLevelLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        riskLevelLabel.setTextFill(Color.web(color));

        HBox.setHgrow(percentageBox, Priority.ALWAYS);
        percentageBox.getChildren().addAll(percentageLabel, riskLevelLabel);

        card.getChildren().addAll(titleLabel, progressBar, percentageBox);

        return card;
    }

    private VBox createInfoCard(String title, String value, String subtitle, String color) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10;");
        HBox row = new HBox(10);
        Label titleLabel = new Label(title + ":");
        titleLabel.setMinWidth(150);
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        valueLabel.setTextFill(Color.web(color));
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setTextFill(Color.GRAY);
        row.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        card.getChildren().add(row);
        return card;
    }

    private String getRiskLevel(int percentage) {
        if (percentage >= 70) return "HIGH RISK";
        if (percentage >= 50) return "MODERATE RISK";
        if (percentage >= 30) return "LOW-MODERATE RISK";
        return "LOW RISK";
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) return "(Underweight)";
        if (bmi < 25) return "(Normal)";
        if (bmi < 30) return "(Overweight)";
        return "(Obese)";
    }

    private String getPriorityColor(String priority) {
        switch (priority.toLowerCase()) {
            case "high": return "#FEE2E2";
            case "medium": return "#FEF3C7";
            default: return "#ECFDF5";
        }
    }

    private String formatKey(String key) {
        return key.replace("_", " ").substring(0, 1).toUpperCase() + key.replace("_", " ").substring(1);
    }

    private String getUnit(String key) {
        if (key.contains("bp")) return " mmHg";
        if (key.contains("glucose")) return " mg/dL";
        if (key.contains("cholesterol")) return " mg/dL";
        return "";
    }

    private VBox createHealthScoreCard(int healthScore) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ECFDF5, #D1FAE5); -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("Overall Health Score");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        ProgressBar scoreBar = new ProgressBar(healthScore / 100.0);
        scoreBar.setPrefWidth(Double.MAX_VALUE);
        scoreBar.setPrefHeight(30);
        String scoreColor = healthScore >= 70 ? "#22C55E" : healthScore >= 50 ? "#F59E0B" : "#EF4444";
        scoreBar.setStyle("-fx-accent: " + scoreColor + ";");

        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreLabel = new Label(healthScore + "/100");
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        scoreLabel.setTextFill(Color.web(scoreColor));
        Label statusLabel = new Label(getHealthStatus(healthScore));
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web(scoreColor));
        scoreBox.getChildren().addAll(scoreLabel, statusLabel);

        card.getChildren().addAll(titleLabel, scoreBar, scoreBox);
        return card;
    }

    private String getHealthStatus(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 70) return "GOOD";
        if (score >= 50) return "MODERATE";
        return "NEEDS IMPROVEMENT";
    }

    private VBox createRiskTrendCard(Map<String, Object> riskTrend) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("5-Year Risk Trend Prediction");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0F766E"));

        // Display trend for each disease
        for (Map.Entry<String, Object> entry : riskTrend.entrySet()) {
            String disease = entry.getKey();
            Object trendData = entry.getValue();
            
            if (trendData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> trend = (Map<String, Object>) trendData;
                
                Integer current = ((Number) trend.get("current")).intValue();
                Integer predicted = ((Number) trend.get("predicted_5_years")).intValue();
                String trendDirection = (String) trend.get("trend");
                
                VBox trendItem = new VBox(5);
                HBox trendRow = new HBox(10);
                trendRow.setAlignment(Pos.CENTER_LEFT);
                
                Label diseaseLabel = new Label(formatDiseaseName(disease) + ":");
                diseaseLabel.setMinWidth(150);
                diseaseLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                
                Label currentLabel = new Label("Current: " + current + "%");
                Label predictedLabel = new Label("→ 5 Years: " + predicted + "%");
                predictedLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                
                String trendColor = trendDirection.equals("increasing") ? "#EF4444" : "#22C55E";
                predictedLabel.setTextFill(Color.web(trendColor));
                
                Label trendLabel = new Label("(" + trendDirection.toUpperCase() + ")");
                trendLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                trendLabel.setTextFill(Color.web(trendColor));
                
                trendRow.getChildren().addAll(diseaseLabel, currentLabel, predictedLabel, trendLabel);
                trendItem.getChildren().add(trendRow);
                card.getChildren().add(trendItem);
            }
        }

        return card;
    }

    private String formatDiseaseName(String disease) {
        return disease.replace("_", " ").substring(0, 1).toUpperCase() + disease.replace("_", " ").substring(1);
    }

    private VBox createPopulationComparisonCard(Map<String, Object> comparison) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("Population Comparison");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0F766E"));
        
        String ageGroup = (String) comparison.get("age_group");
        Label subtitleLabel = new Label("Compared to average " + ageGroup + " age group");
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setTextFill(Color.GRAY);
        card.getChildren().addAll(titleLabel, subtitleLabel);

        // Compare each disease
        String[] diseases = {"diabetes", "heart_disease", "hypertension"};
        String[] diseaseNames = {"Diabetes", "Heart Disease", "Hypertension"};
        
        for (int i = 0; i < diseases.length; i++) {
            Object diseaseData = comparison.get(diseases[i]);
            if (diseaseData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) diseaseData;
                
                VBox diseaseBox = new VBox(8);
                diseaseBox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 12; -fx-background-radius: 8;");
                
                HBox headerBox = new HBox(10);
                headerBox.setAlignment(Pos.CENTER_LEFT);
                
                Label diseaseLabel = new Label(diseaseNames[i] + ":");
                diseaseLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                diseaseLabel.setMinWidth(120);
                
                Integer yourRisk = ((Number) data.get("your_risk")).intValue();
                Integer popAvg = ((Number) data.get("population_avg")).intValue();
                Integer difference = ((Number) data.get("difference")).intValue();
                Integer percentile = ((Number) data.get("percentile")).intValue();
                String status = (String) data.get("status");
                
                Label yourLabel = new Label("You: " + yourRisk + "%");
                yourLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                
                Label popLabel = new Label("Avg: " + popAvg + "%");
                popLabel.setFont(Font.font("System", 12));
                popLabel.setTextFill(Color.GRAY);
                
                String diffColor = difference > 0 ? "#EF4444" : difference < 0 ? "#22C55E" : "#6B7280";
                String diffText = difference > 0 ? "+" + difference : String.valueOf(difference);
                Label diffLabel = new Label(diffText + "%");
                diffLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                diffLabel.setTextFill(Color.web(diffColor));
                
                Label percentileLabel = new Label(percentile + "th percentile");
                percentileLabel.setFont(Font.font("System", 11));
                percentileLabel.setTextFill(Color.web("#6B7280"));
                
                String statusColor = status.equals("above_average") ? "#EF4444" : 
                                    status.equals("below_average") ? "#22C55E" : "#6B7280";
                Label statusBadge = new Label(status.replace("_", " ").toUpperCase());
                statusBadge.setStyle("-fx-background-color: " + statusColor + "20; -fx-text-fill: " + statusColor + "; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: bold;");
                
                headerBox.getChildren().addAll(diseaseLabel, yourLabel, popLabel, diffLabel, percentileLabel, statusBadge);
                diseaseBox.getChildren().add(headerBox);
                
                // Progress bars comparison
                HBox progressBox = new HBox(10);
                progressBox.setAlignment(Pos.CENTER_LEFT);
                
                VBox yourProgressBox = new VBox(3);
                Label yourLabel2 = new Label("Your Risk");
                yourLabel2.setFont(Font.font("System", 10));
                ProgressBar yourBar = new ProgressBar(yourRisk / 100.0);
                yourBar.setPrefWidth(150);
                yourBar.setPrefHeight(12);
                yourBar.setStyle("-fx-accent: " + (yourRisk >= 70 ? "#EF4444" : yourRisk >= 50 ? "#F59E0B" : "#22C55E") + ";");
                yourProgressBox.getChildren().addAll(yourLabel2, yourBar);
                
                VBox popProgressBox = new VBox(3);
                Label popLabel2 = new Label("Population Avg");
                popLabel2.setFont(Font.font("System", 10));
                ProgressBar popBar = new ProgressBar(popAvg / 100.0);
                popBar.setPrefWidth(150);
                popBar.setPrefHeight(12);
                popBar.setStyle("-fx-accent: #6B7280;");
                popProgressBox.getChildren().addAll(popLabel2, popBar);
                
                progressBox.getChildren().addAll(yourProgressBox, popProgressBox);
                diseaseBox.getChildren().add(progressBox);
                
                card.getChildren().add(diseaseBox);
            }
        }

        return card;
    }

    private VBox createRiskExplanationsCard(Map<String, Object> explanations) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("Risk Explanations");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0F766E"));

        for (Map.Entry<String, Object> entry : explanations.entrySet()) {
            String disease = entry.getKey();
            Object expData = entry.getValue();
            
            if (expData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> exp = (Map<String, Object>) expData;
                
                VBox diseaseBox = new VBox(8);
                diseaseBox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 15; -fx-background-radius: 8;");
                
                HBox headerBox = new HBox(10);
                Label diseaseLabel = new Label(formatDiseaseName(disease) + " (" + exp.get("risk_level") + "%)");
                diseaseLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                String level = (String) exp.get("level");
                String levelColor = level.equals("high") ? "#EF4444" : level.equals("moderate") ? "#F59E0B" : "#22C55E";
                diseaseLabel.setTextFill(Color.web(levelColor));
                
                Label levelBadge = new Label(level.toUpperCase());
                levelBadge.setStyle("-fx-background-color: " + levelColor + "20; -fx-text-fill: " + levelColor + "; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                headerBox.getChildren().addAll(diseaseLabel, levelBadge);
                
                Label explanationLabel = new Label((String) exp.get("explanation"));
                explanationLabel.setWrapText(true);
                explanationLabel.setFont(Font.font("System", 13));
                
                @SuppressWarnings("unchecked")
                List<String> factors = (List<String>) exp.get("primary_factors");
                if (factors != null && !factors.isEmpty()) {
                    VBox factorsBox = new VBox(5);
                    Label factorsLabel = new Label("Key Factors:");
                    factorsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                    factorsBox.getChildren().add(factorsLabel);
                    for (String factor : factors) {
                        Label factorLabel = new Label("• " + factor);
                        factorLabel.setFont(Font.font("System", 12));
                        factorLabel.setTextFill(Color.web("#4B5563"));
                        factorsBox.getChildren().add(factorLabel);
                    }
                    diseaseBox.getChildren().addAll(headerBox, explanationLabel, factorsBox);
                } else {
                    diseaseBox.getChildren().addAll(headerBox, explanationLabel);
                }
                
                card.getChildren().add(diseaseBox);
            }
        }

        return card;
    }

    private VBox createFeatureImportanceCard(Map<String, Object> importance) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("Top Contributing Factors");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0F766E"));
        Label subtitleLabel = new Label("Factors most influencing your risk predictions");
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setTextFill(Color.GRAY);
        card.getChildren().addAll(titleLabel, subtitleLabel);

        for (Map.Entry<String, Object> entry : importance.entrySet()) {
            String disease = entry.getKey();
            Object featuresData = entry.getValue();
            
            if (featuresData instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> features = (List<Map<String, Object>>) featuresData;
                
                VBox diseaseBox = new VBox(8);
                Label diseaseLabel = new Label(formatDiseaseName(disease) + ":");
                diseaseLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                diseaseBox.getChildren().add(diseaseLabel);
                
                for (Map<String, Object> feature : features) {
                    HBox featureRow = new HBox(10);
                    featureRow.setAlignment(Pos.CENTER_LEFT);
                    
                    String featureName = (String) feature.get("feature");
                    Double importanceVal = ((Number) feature.get("importance")).doubleValue();
                    
                    Label featureLabel = new Label(formatFeatureName(featureName) + ":");
                    featureLabel.setMinWidth(150);
                    featureLabel.setFont(Font.font("System", 12));
                    
                    ProgressBar importanceBar = new ProgressBar(importanceVal);
                    importanceBar.setPrefWidth(150);
                    importanceBar.setPrefHeight(8);
                    importanceBar.setStyle("-fx-accent: #3B82F6;");
                    
                    Label percentLabel = new Label(String.format("%.1f%%", importanceVal * 100));
                    percentLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                    percentLabel.setMinWidth(50);
                    
                    featureRow.getChildren().addAll(featureLabel, importanceBar, percentLabel);
                    diseaseBox.getChildren().add(featureRow);
                }
                
                card.getChildren().add(diseaseBox);
            }
        }

        return card;
    }

    private String formatFeatureName(String feature) {
        return feature.replace("_", " ").substring(0, 1).toUpperCase() + feature.replace("_", " ").substring(1);
    }

    private VBox createRiskReductionCard(Map<String, Object> reductions) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #F0FDF4, #ECFDF5); -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("Potential Risk Reduction");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0F766E"));
        Label subtitleLabel = new Label("Estimated risk reduction from lifestyle improvements");
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setTextFill(Color.GRAY);
        card.getChildren().addAll(titleLabel, subtitleLabel);

        for (Map.Entry<String, Object> entry : reductions.entrySet()) {
            String scenario = entry.getKey();
            Object scenarioData = entry.getValue();
            
            if (scenarioData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> scenarioMap = (Map<String, Object>) scenarioData;
                
                VBox scenarioBox = new VBox(10);
                scenarioBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");
                
                Label scenarioLabel = new Label(formatScenarioName(scenario));
                scenarioLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                scenarioLabel.setTextFill(Color.web("#059669"));
                
                Label descLabel = new Label((String) scenarioMap.get("description"));
                descLabel.setWrapText(true);
                descLabel.setFont(Font.font("System", 12));
                
                HBox reductionBox = new HBox(15);
                reductionBox.setAlignment(Pos.CENTER_LEFT);
                
                Integer diabetesRed = ((Number) scenarioMap.get("diabetes")).intValue();
                Integer heartRed = ((Number) scenarioMap.get("heart_disease")).intValue();
                Integer hypertensionRed = ((Number) scenarioMap.get("hypertension")).intValue();
                
                if (diabetesRed > 0) {
                    Label redLabel = new Label("Diabetes: -" + diabetesRed + "%");
                    redLabel.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 5 10; -fx-background-radius: 6; -fx-font-weight: bold;");
                    reductionBox.getChildren().add(redLabel);
                }
                if (heartRed > 0) {
                    Label redLabel = new Label("Heart: -" + heartRed + "%");
                    redLabel.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 5 10; -fx-background-radius: 6; -fx-font-weight: bold;");
                    reductionBox.getChildren().add(redLabel);
                }
                if (hypertensionRed > 0) {
                    Label redLabel = new Label("BP: -" + hypertensionRed + "%");
                    redLabel.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 5 10; -fx-background-radius: 6; -fx-font-weight: bold;");
                    reductionBox.getChildren().add(redLabel);
                }
                
                scenarioBox.getChildren().addAll(scenarioLabel, descLabel, reductionBox);
                card.getChildren().add(scenarioBox);
            }
        }

        return card;
    }

    private String formatScenarioName(String scenario) {
        return scenario.replace("_", " ").substring(0, 1).toUpperCase() + scenario.replace("_", " ").substring(1);
    }

    private VBox createActionPlanCard(List<Map<String, Object>> actionPlan) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("Personalized Action Plan");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#0F766E"));

        for (Map<String, Object> action : actionPlan) {
            VBox actionBox = new VBox(10);
            String category = (String) action.get("category");
            String categoryColor = getCategoryColor(category);
            actionBox.setStyle("-fx-background-color: " + categoryColor + "15; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: " + categoryColor + "; -fx-border-width: 2;");
            
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            
            Label priorityLabel = new Label("#" + action.get("priority"));
            priorityLabel.setStyle("-fx-background-color: " + categoryColor + "; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-min-width: 40;");
            
            Label titleLabel2 = new Label((String) action.get("title"));
            titleLabel2.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleLabel2.setTextFill(Color.web(categoryColor));
            
            Label impactLabel = new Label((String) action.get("impact") + " Impact");
            impactLabel.setStyle("-fx-background-color: " + categoryColor + "30; -fx-text-fill: " + categoryColor + "; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: bold;");
            
            headerBox.getChildren().addAll(priorityLabel, titleLabel2, impactLabel);
            
            Label descLabel = new Label((String) action.get("description"));
            descLabel.setWrapText(true);
            descLabel.setFont(Font.font("System", 12));
            
            Label timelineLabel = new Label("⏱ Timeline: " + action.get("timeline"));
            timelineLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            timelineLabel.setTextFill(Color.web("#6B7280"));
            
            actionBox.getChildren().addAll(headerBox, descLabel, timelineLabel);
            
            @SuppressWarnings("unchecked")
            List<String> steps = (List<String>) action.get("steps");
            if (steps != null && !steps.isEmpty()) {
                VBox stepsBox = new VBox(5);
                stepsBox.setStyle("-fx-padding: 10 0 0 0;");
                Label stepsLabel = new Label("Steps:");
                stepsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                stepsBox.getChildren().add(stepsLabel);
                for (String step : steps) {
                    Label stepLabel = new Label("→ " + step);
                    stepLabel.setWrapText(true);
                    stepLabel.setFont(Font.font("System", 11));
                    stepLabel.setTextFill(Color.web("#4B5563"));
                    stepsBox.getChildren().add(stepLabel);
                }
                actionBox.getChildren().add(stepsBox);
            }
            
            card.getChildren().add(actionBox);
        }

        return card;
    }

    private String getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "immediate": return "#EF4444";
            case "lifestyle": return "#F59E0B";
            case "exercise": return "#3B82F6";
            case "nutrition": return "#10B981";
            case "wellness": return "#8B5CF6";
            case "monitoring": return "#6366F1";
            default: return "#6B7280";
        }
    }

    private VBox createScreeningCard(List<Map<String, Object>> screenings) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setMinWidth(450);

        Label titleLabel = new Label("Recommended Health Screenings");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0F766E"));
        Label subtitleLabel = new Label("Based on your risk profile");
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setTextFill(Color.GRAY);
        card.getChildren().addAll(titleLabel, subtitleLabel);

        for (Map<String, Object> screening : screenings) {
            VBox screeningBox = new VBox(8);
            screeningBox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 12; -fx-background-radius: 8;");
            
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            
            Label testLabel = new Label((String) screening.get("test"));
            testLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
            
            String priority = (String) screening.get("priority");
            String priorityColor = priority.equals("high") ? "#EF4444" : "#F59E0B";
            Label priorityBadge = new Label(priority.toUpperCase());
            priorityBadge.setStyle("-fx-background-color: " + priorityColor + "20; -fx-text-fill: " + priorityColor + "; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: bold;");
            
            headerBox.getChildren().addAll(testLabel, priorityBadge);
            
            Label frequencyLabel = new Label("Frequency: " + screening.get("frequency"));
            frequencyLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            frequencyLabel.setTextFill(Color.web("#059669"));
            
            Label purposeLabel = new Label("Purpose: " + screening.get("purpose"));
            purposeLabel.setWrapText(true);
            purposeLabel.setFont(Font.font("System", 11));
            purposeLabel.setTextFill(Color.web("#6B7280"));
            
            screeningBox.getChildren().addAll(headerBox, frequencyLabel, purposeLabel);
            card.getChildren().add(screeningBox);
        }

        return card;
    }

    private void exportResults() {
        if (currentAssessment == null) {
            showError("No Results", "No risk assessment results to export.");
            return;
        }
        
        String report = ReportFormatter.formatRiskAssessment(
            currentAssessment,
            lastAge, lastWeight, lastHeight,
            lastSymptoms != null ? lastSymptoms : new ArrayList<>(),
            lastFamilyHistory != null ? lastFamilyHistory : new ArrayList<>()
        );
        
        FileExporter.exportToFile(
            report,
            "risk_assessment",
            "Export Risk Assessment Report",
            (Stage) getScene().getWindow()
        );
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Risk assessment report exported successfully!"
        );
    }

    private void exportResultsJSON() {
        if (currentAssessment == null) {
            showError("No Results", "No risk assessment results to export.");
            return;
        }
        
        try {
            java.util.Map<String, Object> exportData = new java.util.HashMap<>();
            exportData.put("timestamp", new java.util.Date().toString());
            exportData.put("personal_info", java.util.Map.of(
                "age", lastAge,
                "weight", lastWeight,
                "height", lastHeight,
                "bmi", currentAssessment.getBmi(),
                "symptoms", lastSymptoms != null ? lastSymptoms : new java.util.ArrayList<>(),
                "family_history", lastFamilyHistory != null ? lastFamilyHistory : new java.util.ArrayList<>()
            ));
            exportData.put("risks", java.util.Map.of(
                "diabetes", currentAssessment.getDiabetesRisk(),
                "heart_disease", currentAssessment.getHeartRisk(),
                "hypertension", currentAssessment.getHypertensionRisk()
            ));
            exportData.put("health_score", currentAssessment.getHealthScore());
            exportData.put("assessment", objectMapper.convertValue(currentAssessment, java.util.Map.class));
            
            JSONExporter.exportToJSON(
                exportData,
                "risk_assessment",
                "Export Risk Assessment as JSON",
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

    private void copyResults() {
        if (currentAssessment == null) {
            showError("No Results", "No risk assessment results to copy.");
            return;
        }
        
        String report = ReportFormatter.formatRiskAssessment(
            currentAssessment,
            lastAge, lastWeight, lastHeight,
            lastSymptoms != null ? lastSymptoms : new ArrayList<>(),
            lastFamilyHistory != null ? lastFamilyHistory : new ArrayList<>()
        );
        
        FileExporter.copyToClipboard(report);
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Results copied to clipboard!"
        );
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


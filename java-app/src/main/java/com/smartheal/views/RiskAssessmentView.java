package com.smartheal.views;

import com.smartheal.api.AdvancedApiClient;
import com.smartheal.api.ApiClient;
import com.smartheal.api.HealthCoachApiClient;
import com.smartheal.dao.HistoryDAO;
import com.smartheal.models.RiskAssessment;
import com.smartheal.models.Recommendation;
import com.smartheal.models.Symptom;
import com.smartheal.components.ConfidenceIndicator;
import com.smartheal.utils.NotificationHelper;
import com.smartheal.utils.FileExporter;
import com.smartheal.utils.ReportFormatter;
import com.smartheal.utils.JSONExporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

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
    private TextField vegetableServingsField;
    private TextField processedMealsField;
    private TextField hydrationField;
    private ComboBox<String> caffeineIntakeCombo;
    private TextField moderateActivityField;
    private TextField vigorousActivityField;
    private TextField strengthSessionsField;
    private TextField sedentaryHoursField;
    private ComboBox<String> sleepQualityCombo;
    private ComboBox<String> sleepConsistencyCombo;
    private CheckBox snoringCheckBox;
    private ComboBox<String> stressCopingCombo;
    private TextField workHoursField;
    private ComboBox<String> moodStabilityCombo;
    private ComboBox<String> medicationAdherenceCombo;
    private CheckBox medicationSideEffectsCheckBox;
    private ComboBox<String> smokingIntensityCombo;
    private CheckBox vapingCheckBox;
    private ComboBox<String> environmentExposureCombo;
    private CheckBox shiftWorkCheckBox;
    private TextField lastCheckupField;
    private ComboBox<String> vaccinationStatusCombo;
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
    private VBox confidenceIndicators;
    private HBox uncertaintyAlert;
    private boolean healthCoachPlanShown;
    private VBox healthCoachSummaryBox;
    private VBox lifestyleInsightsBox;
    private VBox dataQualitySummaryBox;
    private JSONObject lastHealthCoachPlan;
    private JSONObject lastAdvancedResponse;
    private Map<String, Object> lastLifestyleInputs;
    
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
        this.healthCoachPlanShown = false;
        this.lastHealthCoachPlan = null;
        this.lastAdvancedResponse = null;
        this.lastLifestyleInputs = new HashMap<>();

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

        Label nutritionDetailLabel = new Label("Nutrition & Hydration Details");
        nutritionDetailLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        nutritionDetailLabel.setTextFill(Color.web("#0F172A"));

        HBox vegetableBox = new HBox(10);
        vegetableBox.setAlignment(Pos.CENTER_LEFT);
        Label vegetableLabel = new Label("Fruit & Veg servings/day:");
        vegetableLabel.setMinWidth(180);
        vegetableServingsField = new TextField();
        vegetableServingsField.setPromptText("e.g., 4");
        vegetableServingsField.setPrefWidth(120);
        vegetableBox.getChildren().addAll(vegetableLabel, vegetableServingsField);

        HBox processedBox = new HBox(10);
        processedBox.setAlignment(Pos.CENTER_LEFT);
        Label processedLabel = new Label("Processed meals/week:");
        processedLabel.setMinWidth(180);
        processedMealsField = new TextField();
        processedMealsField.setPromptText("e.g., 2");
        processedMealsField.setPrefWidth(120);
        processedBox.getChildren().addAll(processedLabel, processedMealsField);

        HBox hydrationBox = new HBox(10);
        hydrationBox.setAlignment(Pos.CENTER_LEFT);
        Label hydrationLabel = new Label("Water intake (glasses/day):");
        hydrationLabel.setMinWidth(180);
        hydrationField = new TextField();
        hydrationField.setPromptText("e.g., 8");
        hydrationField.setPrefWidth(120);
        hydrationBox.getChildren().addAll(hydrationLabel, hydrationField);

        HBox caffeineBox = new HBox(10);
        caffeineBox.setAlignment(Pos.CENTER_LEFT);
        Label caffeineLabel = new Label("Caffeine intake:");
        caffeineLabel.setMinWidth(180);
        caffeineIntakeCombo = new ComboBox<>();
        caffeineIntakeCombo.getItems().addAll("None", "Low (≤1 cup/day)", "Moderate (2 cups/day)", "High (3+ cups/day)");
        caffeineIntakeCombo.setValue("Moderate (2 cups/day)");
        caffeineIntakeCombo.setPrefWidth(200);
        caffeineBox.getChildren().addAll(caffeineLabel, caffeineIntakeCombo);

        Label activityDetailLabel = new Label("Physical Activity & Sedentary Pattern");
        activityDetailLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        activityDetailLabel.setTextFill(Color.web("#0F172A"));

        HBox moderateActivityBox = new HBox(10);
        moderateActivityBox.setAlignment(Pos.CENTER_LEFT);
        Label moderateActivityLabel = new Label("Moderate activity (min/week):");
        moderateActivityLabel.setMinWidth(220);
        moderateActivityField = new TextField();
        moderateActivityField.setPromptText("e.g., 120");
        moderateActivityField.setPrefWidth(140);
        moderateActivityBox.getChildren().addAll(moderateActivityLabel, moderateActivityField);

        HBox vigorousActivityBox = new HBox(10);
        vigorousActivityBox.setAlignment(Pos.CENTER_LEFT);
        Label vigorousActivityLabel = new Label("Vigorous activity (min/week):");
        vigorousActivityLabel.setMinWidth(220);
        vigorousActivityField = new TextField();
        vigorousActivityField.setPromptText("e.g., 45");
        vigorousActivityField.setPrefWidth(140);
        vigorousActivityBox.getChildren().addAll(vigorousActivityLabel, vigorousActivityField);

        HBox strengthBox = new HBox(10);
        strengthBox.setAlignment(Pos.CENTER_LEFT);
        Label strengthLabel = new Label("Strength sessions/week:");
        strengthLabel.setMinWidth(220);
        strengthSessionsField = new TextField();
        strengthSessionsField.setPromptText("e.g., 2");
        strengthSessionsField.setPrefWidth(140);
        strengthBox.getChildren().addAll(strengthLabel, strengthSessionsField);

        HBox sedentaryBox = new HBox(10);
        sedentaryBox.setAlignment(Pos.CENTER_LEFT);
        Label sedentaryLabel = new Label("Sitting time (hours/day):");
        sedentaryLabel.setMinWidth(220);
        sedentaryHoursField = new TextField();
        sedentaryHoursField.setPromptText("e.g., 8");
        sedentaryHoursField.setPrefWidth(140);
        sedentaryBox.getChildren().addAll(sedentaryLabel, sedentaryHoursField);

        Label sleepDetailLabel = new Label("Sleep Quality & Recovery");
        sleepDetailLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        sleepDetailLabel.setTextFill(Color.web("#0F172A"));

        HBox sleepQualityBox = new HBox(10);
        sleepQualityBox.setAlignment(Pos.CENTER_LEFT);
        Label sleepQualityLabel = new Label("Sleep quality rating:");
        sleepQualityLabel.setMinWidth(180);
        sleepQualityCombo = new ComboBox<>();
        sleepQualityCombo.getItems().addAll("Poor", "Fair", "Good", "Excellent");
        sleepQualityCombo.setValue("Good");
        sleepQualityCombo.setPrefWidth(180);
        sleepQualityBox.getChildren().addAll(sleepQualityLabel, sleepQualityCombo);

        HBox sleepConsistencyBox = new HBox(10);
        sleepConsistencyBox.setAlignment(Pos.CENTER_LEFT);
        Label sleepConsistencyLabel = new Label("Sleep schedule consistency:");
        sleepConsistencyLabel.setMinWidth(220);
        sleepConsistencyCombo = new ComboBox<>();
        sleepConsistencyCombo.getItems().addAll("Irregular", "Somewhat regular", "Consistent", "Highly consistent");
        sleepConsistencyCombo.setValue("Somewhat regular");
        sleepConsistencyCombo.setPrefWidth(200);
        sleepConsistencyBox.getChildren().addAll(sleepConsistencyLabel, sleepConsistencyCombo);

        snoringCheckBox = new CheckBox("Snoring or suspected apnea symptoms");
        snoringCheckBox.setFont(Font.font("System", 13));

        Label mentalDetailLabel = new Label("Stress & Mental Wellbeing");
        mentalDetailLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        mentalDetailLabel.setTextFill(Color.web("#0F172A"));

        HBox stressCopingBox = new HBox(10);
        stressCopingBox.setAlignment(Pos.CENTER_LEFT);
        Label stressCopingLabel = new Label("Stress coping ability:");
        stressCopingLabel.setMinWidth(200);
        stressCopingCombo = new ComboBox<>();
        stressCopingCombo.getItems().addAll("Struggling", "Managing", "Strong toolkit");
        stressCopingCombo.setValue("Managing");
        stressCopingCombo.setPrefWidth(180);
        stressCopingBox.getChildren().addAll(stressCopingLabel, stressCopingCombo);

        HBox workHoursBox = new HBox(10);
        workHoursBox.setAlignment(Pos.CENTER_LEFT);
        Label workHoursLabel = new Label("Average work hours/week:");
        workHoursLabel.setMinWidth(220);
        workHoursField = new TextField();
        workHoursField.setPromptText("e.g., 45");
        workHoursField.setPrefWidth(140);
        workHoursBox.getChildren().addAll(workHoursLabel, workHoursField);

        HBox moodBox = new HBox(10);
        moodBox.setAlignment(Pos.CENTER_LEFT);
        Label moodLabel = new Label("Mood stability:");
        moodLabel.setMinWidth(180);
        moodStabilityCombo = new ComboBox<>();
        moodStabilityCombo.getItems().addAll("Low", "Variable", "Stable", "Very stable");
        moodStabilityCombo.setValue("Stable");
        moodStabilityCombo.setPrefWidth(160);
        moodBox.getChildren().addAll(moodLabel, moodStabilityCombo);

        Label medicationDetailLabel = new Label("Medication & Preventive Care");
        medicationDetailLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        medicationDetailLabel.setTextFill(Color.web("#0F172A"));

        HBox medicationAdherenceBox = new HBox(10);
        medicationAdherenceBox.setAlignment(Pos.CENTER_LEFT);
        Label medicationAdherenceLabel = new Label("Medication adherence:");
        medicationAdherenceLabel.setMinWidth(200);
        medicationAdherenceCombo = new ComboBox<>();
        medicationAdherenceCombo.getItems().addAll("Rarely follow plan", "Miss doses sometimes", "Mostly adherent", "Always on schedule");
        medicationAdherenceCombo.setValue("Mostly adherent");
        medicationAdherenceCombo.setPrefWidth(220);
        medicationAdherenceBox.getChildren().addAll(medicationAdherenceLabel, medicationAdherenceCombo);

        medicationSideEffectsCheckBox = new CheckBox("Experiencing medication side effects");
        medicationSideEffectsCheckBox.setFont(Font.font("System", 13));

        HBox lastCheckupBox = new HBox(10);
        lastCheckupBox.setAlignment(Pos.CENTER_LEFT);
        Label lastCheckupLabel = new Label("Last full check-up (months):");
        lastCheckupLabel.setMinWidth(220);
        lastCheckupField = new TextField();
        lastCheckupField.setPromptText("e.g., 12");
        lastCheckupField.setPrefWidth(140);
        lastCheckupBox.getChildren().addAll(lastCheckupLabel, lastCheckupField);

        HBox vaccinationBox = new HBox(10);
        vaccinationBox.setAlignment(Pos.CENTER_LEFT);
        Label vaccinationLabel = new Label("Vaccination status:");
        vaccinationLabel.setMinWidth(200);
        vaccinationStatusCombo = new ComboBox<>();
        vaccinationStatusCombo.getItems().addAll("Unsure / overdue", "Partially up to date", "Fully up to date");
        vaccinationStatusCombo.setValue("Partially up to date");
        vaccinationStatusCombo.setPrefWidth(200);
        vaccinationBox.getChildren().addAll(vaccinationLabel, vaccinationStatusCombo);

        Label exposureDetailLabel = new Label("Substance & Environment Exposure");
        exposureDetailLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        exposureDetailLabel.setTextFill(Color.web("#0F172A"));

        HBox smokingIntensityBox = new HBox(10);
        smokingIntensityBox.setAlignment(Pos.CENTER_LEFT);
        Label smokingIntensityLabel = new Label("Smoking intensity:");
        smokingIntensityLabel.setMinWidth(200);
        smokingIntensityCombo = new ComboBox<>();
        smokingIntensityCombo.getItems().addAll("None", "Occasional (<5/day)", "Daily (5-10/day)", "Heavy (>10/day)");
        smokingIntensityCombo.setValue("None");
        smokingIntensityCombo.setPrefWidth(200);
        smokingIntensityBox.getChildren().addAll(smokingIntensityLabel, smokingIntensityCombo);

        vapingCheckBox = new CheckBox("Currently vaping or using e-cigarettes");
        vapingCheckBox.setFont(Font.font("System", 13));

        HBox environmentExposureBox = new HBox(10);
        environmentExposureBox.setAlignment(Pos.CENTER_LEFT);
        Label environmentExposureLabel = new Label("Work/environment exposure:");
        environmentExposureLabel.setMinWidth(220);
        environmentExposureCombo = new ComboBox<>();
        environmentExposureCombo.getItems().addAll("Low exposure", "Moderate exposure", "High exposure");
        environmentExposureCombo.setValue("Moderate exposure");
        environmentExposureCombo.setPrefWidth(200);
        environmentExposureBox.getChildren().addAll(environmentExposureLabel, environmentExposureCombo);

        shiftWorkCheckBox = new CheckBox("Shift work / rotating night shifts");
        shiftWorkCheckBox.setFont(Font.font("System", 13));

        lifestyleBox.getChildren().addAll(
            lifestyleLabel,
            smokingCheckBox,
            smokingIntensityBox,
            vapingCheckBox,
            exerciseBox,
            alcoholBox,
            dietBox,
            nutritionDetailLabel,
            vegetableBox,
            processedBox,
            hydrationBox,
            caffeineBox,
            activityDetailLabel,
            moderateActivityBox,
            vigorousActivityBox,
            strengthBox,
            sedentaryBox,
            sleepDetailLabel,
            sleepBox,
            sleepQualityBox,
            sleepConsistencyBox,
            snoringCheckBox,
            mentalDetailLabel,
            stressBox,
            stressCopingBox,
            workHoursBox,
            moodBox,
            medicationDetailLabel,
            medicationAdherenceBox,
            medicationSideEffectsCheckBox,
            lastCheckupBox,
            vaccinationBox,
            exposureDetailLabel,
            environmentExposureBox,
            shiftWorkCheckBox
        );

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
            healthCoachPlanShown = false;

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

            Map<String, Object> lifestyleInputs = collectLifestyleInputs();
            if (lifestyleInputs == null) {
                assessButton.setDisable(false);
                loadingIndicator.setVisible(false);
                return;
            }

            // Make variables final for lambda
            final int finalAge = age;
            final double finalWeight = weight;
            final double finalHeight = height;
            final Map<String, Object> finalLifestyleInputs = new HashMap<>(lifestyleInputs);
            if (sleepHours != null) {
                finalLifestyleInputs.put("sleep_hours", sleepHours);
            }
            finalLifestyleInputs.put("stress_level", stressLevel);
            finalLifestyleInputs.put("diet_quality", dietQuality);
            finalLifestyleInputs.put("exercise", exerciseLevel);
            finalLifestyleInputs.put("alcohol", alcoholLevel);
            finalLifestyleInputs.put("smoking", smokingCheckBox.isSelected() ? 1 : 0);

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
                        lastLifestyleInputs.clear();
                        lastLifestyleInputs.putAll(finalLifestyleInputs);
                        
                        displayResults(assessment);
                        requestAdvancedAssessment(
                            assessment,
                            finalAge,
                            finalWeight,
                            finalHeight,
                            sleepHours,
                            stressLevel,
                            dietQuality,
                            exerciseLevel,
                            alcoholLevel,
                            smokingCheckBox.isSelected(),
                            finalLifestyleInputs
                        );
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

    private Double parseOptionalDouble(TextField field, double min, double max, String fieldName) {
        String text = field.getText() != null ? field.getText().trim() : "";
        if (text.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            if (value < min || value > max) {
                showError("Invalid " + fieldName, fieldName + " must be between " + min + " and " + max + ".");
                throw new IllegalArgumentException("Invalid " + fieldName);
            }
            return value;
        } catch (NumberFormatException ex) {
            showError("Invalid " + fieldName, "Please enter a valid number for " + fieldName + ".");
            throw new IllegalArgumentException("Invalid " + fieldName);
        }
    }

    private Integer parseOptionalInteger(TextField field, int min, int max, String fieldName) {
        String text = field.getText() != null ? field.getText().trim() : "";
        if (text.isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text);
            if (value < min || value > max) {
                showError("Invalid " + fieldName, fieldName + " must be between " + min + " and " + max + ".");
                throw new IllegalArgumentException("Invalid " + fieldName);
            }
            return value;
        } catch (NumberFormatException ex) {
            showError("Invalid " + fieldName, "Please enter a valid whole number for " + fieldName + ".");
            throw new IllegalArgumentException("Invalid " + fieldName);
        }
    }

    private Map<String, Object> collectLifestyleInputs() {
        Map<String, Object> data = new HashMap<>();
        try {
            Double vegetables = parseOptionalDouble(vegetableServingsField, 0, 12, "Fruit & vegetable servings per day");
            if (vegetables != null) {
                data.put("vegetable_servings", vegetables);
            }

            Integer processedMeals = parseOptionalInteger(processedMealsField, 0, 30, "Processed meals per week");
            if (processedMeals != null) {
                data.put("processed_meals_per_week", processedMeals);
            }

            Double hydration = parseOptionalDouble(hydrationField, 0, 20, "Water intake (glasses per day)");
            if (hydration != null) {
                data.put("hydration_glasses", hydration);
            }

            Integer moderateActivity = parseOptionalInteger(moderateActivityField, 0, 1000, "Moderate activity minutes");
            if (moderateActivity != null) {
                data.put("moderate_activity_minutes", moderateActivity);
            }

            Integer vigorousActivity = parseOptionalInteger(vigorousActivityField, 0, 1000, "Vigorous activity minutes");
            if (vigorousActivity != null) {
                data.put("vigorous_activity_minutes", vigorousActivity);
            }

            Integer strengthSessions = parseOptionalInteger(strengthSessionsField, 0, 28, "Strength sessions per week");
            if (strengthSessions != null) {
                data.put("strength_training_sessions", strengthSessions);
            }

            Double sedentaryHours = parseOptionalDouble(sedentaryHoursField, 0, 24, "Sitting time (hours per day)");
            if (sedentaryHours != null) {
                data.put("sedentary_hours", sedentaryHours);
            }

            Double workHours = parseOptionalDouble(workHoursField, 0, 120, "Work hours per week");
            if (workHours != null) {
                data.put("work_hours", workHours);
            }

            Integer lastCheckupMonths = parseOptionalInteger(lastCheckupField, 0, 300, "Months since last check-up");
            if (lastCheckupMonths != null) {
                data.put("last_checkup_months", lastCheckupMonths);
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }

        data.put("caffeine_intake", Math.max(0, caffeineIntakeCombo.getSelectionModel().getSelectedIndex()));
        data.put("sleep_quality", Math.max(0, sleepQualityCombo.getSelectionModel().getSelectedIndex()));
        data.put("sleep_consistency", Math.max(0, sleepConsistencyCombo.getSelectionModel().getSelectedIndex()));
        data.put("snoring", snoringCheckBox.isSelected() ? 1 : 0);
        data.put("stress_coping", Math.max(0, stressCopingCombo.getSelectionModel().getSelectedIndex()));
        data.put("mood_stability", Math.max(0, moodStabilityCombo.getSelectionModel().getSelectedIndex()));
        data.put("medication_adherence", Math.max(0, medicationAdherenceCombo.getSelectionModel().getSelectedIndex()));
        data.put("medication_side_effects", medicationSideEffectsCheckBox.isSelected() ? 1 : 0);

        int smokingIntensityIndex = Math.max(0, smokingIntensityCombo.getSelectionModel().getSelectedIndex());
        data.put("smoking_intensity", smokingCheckBox.isSelected() ? smokingIntensityIndex : 0);
        data.put("vaping", vapingCheckBox.isSelected() ? 1 : 0);
        data.put("environmental_exposure", Math.max(0, environmentExposureCombo.getSelectionModel().getSelectedIndex()));
        data.put("shift_work", shiftWorkCheckBox.isSelected() ? 1 : 0);
        data.put("vaccination_status", Math.max(0, vaccinationStatusCombo.getSelectionModel().getSelectedIndex()));

        return data;
    }

    private VBox createAdvancedSection() {
        confidenceIndicators = new VBox(10);
        confidenceIndicators.setFillWidth(true);
        confidenceIndicators.setStyle("-fx-background-color: #f8fafc; -fx-padding: 10; -fx-background-radius: 8;");

        Label loadingLabel = new Label("Calculating advanced insights...");
        loadingLabel.setWrapText(true);
        loadingLabel.setStyle("-fx-text-fill: #0F766E;");
        confidenceIndicators.getChildren().add(loadingLabel);

        uncertaintyAlert = new HBox(10);
        uncertaintyAlert.setAlignment(Pos.CENTER_LEFT);
        uncertaintyAlert.setPadding(new Insets(12));
        uncertaintyAlert.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-radius: 6;");
        uncertaintyAlert.setVisible(false);

        Label warningIcon = new Label("⚠");
        warningIcon.setStyle("-fx-font-size: 16;");
        Label warningText = new Label("Some predictions have higher uncertainty. Provide additional data for improved accuracy.");
        warningText.setWrapText(true);
        warningText.setStyle("-fx-text-fill: #856404; -fx-font-size: 14;");
        uncertaintyAlert.getChildren().addAll(warningIcon, warningText);

        Label sectionTitle = new Label("Advanced Risk Analysis with Confidence Scores");
        sectionTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0F766E;");

        VBox container = new VBox(12);
        container.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 10;");
        lifestyleInsightsBox = new VBox(12);
        lifestyleInsightsBox.setStyle("-fx-background-color: #F0FDFA; -fx-padding: 14; -fx-background-radius: 10; -fx-border-color: #5EEAD4; -fx-border-radius: 10; -fx-border-width: 1;");
        lifestyleInsightsBox.setVisible(false);
        lifestyleInsightsBox.setManaged(false);

        dataQualitySummaryBox = new VBox(8);
        dataQualitySummaryBox.setStyle("-fx-background-color: #EFF6FF; -fx-padding: 12; -fx-background-radius: 8;");
        dataQualitySummaryBox.setVisible(false);
        dataQualitySummaryBox.setManaged(false);

        healthCoachSummaryBox = new VBox(8);
        healthCoachSummaryBox.setStyle("-fx-background-color: #ECFDF5; -fx-padding: 12; -fx-background-radius: 8;");
        healthCoachSummaryBox.setVisible(false);
        healthCoachSummaryBox.setManaged(false);

        container.getChildren().addAll(sectionTitle, uncertaintyAlert, confidenceIndicators, lifestyleInsightsBox, dataQualitySummaryBox, healthCoachSummaryBox);

        return container;
    }

    private void displayResults(RiskAssessment assessment) {
        resultsContainer.getChildren().clear();
        resultsContainer.getChildren().add(createAdvancedSection());

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

        Label advancedFooter = new Label("Advanced insights are powered by ensemble machine learning models with calibrated confidence intervals.");
        advancedFooter.setWrapText(true);
        advancedFooter.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12;");
        resultsContainer.getChildren().add(advancedFooter);
    }

    private void requestAdvancedAssessment(RiskAssessment assessment,
                                           int age,
                                           double weight,
                                           double height,
                                           Double sleepHours,
                                           int stressLevel,
                                           int dietQuality,
                                           int exerciseLevel,
                                           int alcoholLevel,
                                           boolean smoking,
                                           Map<String, Object> lifestyleInputs) {
        if (confidenceIndicators == null) {
            return;
        }

        confidenceIndicators.getChildren().clear();
        hideHealthCoachSummary();
        if (lifestyleInsightsBox != null) {
            lifestyleInsightsBox.getChildren().clear();
            lifestyleInsightsBox.setVisible(false);
            lifestyleInsightsBox.setManaged(false);
        }
        if (dataQualitySummaryBox != null) {
            dataQualitySummaryBox.getChildren().clear();
            dataQualitySummaryBox.setVisible(false);
            dataQualitySummaryBox.setManaged(false);
        }
        lastAdvancedResponse = null;
        Label loadingLabel = new Label("Calculating advanced insights...");
        loadingLabel.setWrapText(true);
        loadingLabel.setStyle("-fx-text-fill: #0F766E;");
        confidenceIndicators.getChildren().add(loadingLabel);

        JSONObject payload = buildAdvancedPayload(assessment, age, weight, height, sleepHours,
                stressLevel, dietQuality, exerciseLevel, alcoholLevel, smoking, lifestyleInputs);

        CompletableFuture
            .supplyAsync(() -> AdvancedApiClient.getAdvancedRiskAssessment(payload))
            .thenAccept(response -> Platform.runLater(() -> displayAdvancedResults(response)))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    confidenceIndicators.getChildren().clear();
                    Label errorLabel = new Label("Advanced analysis is temporarily unavailable. Please try again later.");
                    errorLabel.setWrapText(true);
                    errorLabel.setStyle("-fx-text-fill: #DC2626;");
                    confidenceIndicators.getChildren().add(errorLabel);

                    if (getScene() != null && getScene().getRoot() instanceof StackPane) {
                        String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        NotificationHelper.showErrorNotification((StackPane) getScene().getRoot(),
                            "Advanced assessment failed: " + message);
                    }
                    if (uncertaintyAlert != null) {
                        uncertaintyAlert.setVisible(false);
                    }
                    healthCoachPlanShown = false;
                });
                return null;
            });
    }

    private JSONObject buildAdvancedPayload(RiskAssessment assessment,
                                            int age,
                                            double weight,
                                            double height,
                                            Double sleepHours,
                                            int stressLevel,
                                            int dietQuality,
                                            int exerciseLevel,
                                            int alcoholLevel,
                                            boolean smoking,
                                            Map<String, Object> lifestyleInputs) {
        JSONObject payload = new JSONObject();
        payload.put("age", age);
        payload.put("weight", weight);
        payload.put("height", height);
        payload.put("bmi", assessment.getBmi());

        Map<String, Double> estimatedValues = assessment.getEstimatedValues();
        if (estimatedValues != null) {
            if (estimatedValues.containsKey("systolic_bp")) {
                payload.put("systolic_bp", estimatedValues.get("systolic_bp"));
                payload.put("blood_pressure", estimatedValues.get("systolic_bp"));
            }
            if (estimatedValues.containsKey("fasting_glucose")) {
                payload.put("glucose", estimatedValues.get("fasting_glucose"));
            }
            if (estimatedValues.containsKey("cholesterol")) {
                payload.put("cholesterol", estimatedValues.get("cholesterol"));
            }
        }

        if (sleepHours != null) {
            payload.put("sleep_hours", sleepHours);
        }
        payload.put("stress_level", stressLevel);
        payload.put("diet_quality", dietQuality);
        payload.put("exercise", exerciseLevel);
        payload.put("alcohol", alcoholLevel);
        payload.put("smoking", smoking ? 1 : 0);

        if (lifestyleInputs != null) {
            for (Map.Entry<String, Object> entry : lifestyleInputs.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    payload.put(entry.getKey(), value);
                }
            }
        }

        payload.put("baseline_diabetes_risk", assessment.getDiabetesRisk());
        payload.put("baseline_heart_disease_risk", assessment.getHeartRisk());
        payload.put("baseline_hypertension_risk", assessment.getHypertensionRisk());

        if (assessment.getHealthScore() != null) {
            payload.put("baseline_health_score", assessment.getHealthScore());
        }

        return payload;
    }

    private void displayAdvancedResults(JSONObject response) {
        if (confidenceIndicators == null) {
            return;
        }

        confidenceIndicators.getChildren().clear();

        if (response == null || response.isEmpty()) {
            Label noData = new Label("Advanced analysis did not return any insights.");
            noData.setWrapText(true);
            noData.setStyle("-fx-text-fill: #334155;");
            confidenceIndicators.getChildren().add(noData);
            if (uncertaintyAlert != null) {
                uncertaintyAlert.setVisible(false);
            }
            if (lifestyleInsightsBox != null) {
                lifestyleInsightsBox.setVisible(false);
                lifestyleInsightsBox.setManaged(false);
            }
            if (dataQualitySummaryBox != null) {
                dataQualitySummaryBox.setVisible(false);
                dataQualitySummaryBox.setManaged(false);
            }
            lastAdvancedResponse = null;
            return;
        }

        lastAdvancedResponse = response;

        JSONObject predictions = response.optJSONObject("predictions");
        if (predictions == null || predictions.isEmpty()) {
            Label noData = new Label("Advanced analysis did not return any insights.");
            noData.setWrapText(true);
            noData.setStyle("-fx-text-fill: #334155;");
            confidenceIndicators.getChildren().add(noData);
            if (uncertaintyAlert != null) {
                uncertaintyAlert.setVisible(false);
            }
            if (lifestyleInsightsBox != null) {
                lifestyleInsightsBox.setVisible(false);
                lifestyleInsightsBox.setManaged(false);
            }
            if (dataQualitySummaryBox != null) {
                dataQualitySummaryBox.setVisible(false);
                dataQualitySummaryBox.setManaged(false);
            }
            return;
        }

        boolean showUncertainty = false;
        String[] preferredOrder = {"diabetes", "heart_disease", "hypertension"};

        for (String disease : preferredOrder) {
            if (predictions.has(disease)) {
                JSONObject prediction = predictions.getJSONObject(disease);
                confidenceIndicators.getChildren().add(new ConfidenceIndicator(disease, prediction));
                double uncertaintyScore = prediction.optDouble("uncertainty_score", 0.0);
                if (uncertaintyScore > 0.3) {
                    showUncertainty = true;
                }
            }
        }

        Iterator<String> remainingKeys = predictions.keys();
        while (remainingKeys.hasNext()) {
            String key = remainingKeys.next();
            boolean alreadyAdded = false;
            for (String disease : preferredOrder) {
                if (disease.equals(key)) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                JSONObject prediction = predictions.getJSONObject(key);
                confidenceIndicators.getChildren().add(new ConfidenceIndicator(key, prediction));
                double uncertaintyScore = prediction.optDouble("uncertainty_score", 0.0);
                if (uncertaintyScore > 0.3) {
                    showUncertainty = true;
                }
            }
        }

        if (confidenceIndicators.getChildren().isEmpty()) {
            Label noData = new Label("Advanced analysis did not return any insights.");
            noData.setWrapText(true);
            noData.setStyle("-fx-text-fill: #334155;");
            confidenceIndicators.getChildren().add(noData);
        }

        if (uncertaintyAlert != null) {
            uncertaintyAlert.setVisible(showUncertainty);
        }

        JSONObject lifestyleSummary = response.optJSONObject("lifestyle_summary");
        JSONArray overallFocus = response.optJSONArray("overall_focus");
        JSONArray protectiveFactors = response.optJSONArray("protective_factors");
        updateLifestyleInsights(lifestyleSummary, overallFocus, protectiveFactors);

        JSONObject dataQuality = response.optJSONObject("data_quality");
        updateDataQualitySummary(dataQuality);

        maybeTriggerHealthCoachPlan(predictions);
    }

    private void updateLifestyleInsights(JSONObject summary, JSONArray focus, JSONArray protectiveFactors) {
        if (lifestyleInsightsBox == null) {
            return;
        }

        lifestyleInsightsBox.getChildren().clear();

        if (summary == null || summary.isEmpty()) {
            lifestyleInsightsBox.setVisible(false);
            lifestyleInsightsBox.setManaged(false);
            return;
        }

        lifestyleInsightsBox.setVisible(true);
        lifestyleInsightsBox.setManaged(true);

        Label header = new Label("Lifestyle & Daily Habits Impact");
        header.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #0f766e;");

        double overallScore = summary.optDouble("overall_score", Double.NaN);
        VBox scoreCard = createOverallLifestyleCard(overallScore, summary.optString("overall_status", "Unknown"));

        lifestyleInsightsBox.getChildren().addAll(header, scoreCard);

        if (focus != null && focus.length() > 0) {
            lifestyleInsightsBox.getChildren().add(createPriorityList("Top Lifestyle Priorities", focus, true));
        }

        if (protectiveFactors != null && protectiveFactors.length() > 0) {
            lifestyleInsightsBox.getChildren().add(createPriorityList("Protective Strengths", protectiveFactors, false));
        }

        JSONObject categories = summary.optJSONObject("categories");
        if (categories != null && categories.length() > 0) {
            VBox categoriesBox = new VBox(10);
            categoriesBox.setStyle("-fx-background-color: rgba(15, 118, 110, 0.05); -fx-padding: 12; -fx-background-radius: 8;");

            Label categoriesTitle = new Label("Category Breakdown");
            categoriesTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
            categoriesBox.getChildren().add(categoriesTitle);

            for (String key : categories.keySet()) {
                JSONObject category = categories.optJSONObject(key);
                if (category != null) {
                    categoriesBox.getChildren().add(createLifestyleCategoryCard(category));
                }
            }

            lifestyleInsightsBox.getChildren().add(categoriesBox);
        }
    }

    private VBox createOverallLifestyleCard(double score, String status) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(14,116,144,0.12), 8, 0, 0, 3);");

        Label title = new Label("Overall Lifestyle Score");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        ProgressBar bar = new ProgressBar(Double.isNaN(score) ? 0 : Math.max(0, Math.min(1, score / 100.0)));
        bar.setPrefWidth(Double.MAX_VALUE);
        String color = getLifestyleScoreColor(score);
        bar.setStyle("-fx-accent: " + color + ";");

        HBox details = new HBox(10);
        details.setAlignment(Pos.CENTER_LEFT);
        Label scoreLabel = new Label(Double.isNaN(score) ? "—" : String.format("%.0f/100", score));
        scoreLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        details.getChildren().addAll(scoreLabel, statusLabel);

        box.getChildren().addAll(title, bar, details);
        return box;
    }

    private VBox createPriorityList(String titleText, JSONArray items, boolean highlight) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: " + (highlight ? "#FEF3C7" : "#ECFEFF") + "; -fx-padding: 10; -fx-background-radius: 8;");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(title);

        int displayCount = Math.min(items.length(), 4);
        for (int i = 0; i < displayCount; i++) {
            JSONObject obj = items.optJSONObject(i);
            if (obj == null) {
                continue;
            }
            String label = obj.optString("label", obj.optString("category", "Focus"));
            String action = obj.optString("recommended_action", obj.optString("insight", ""));
            double itemScore = obj.optDouble("score", Double.NaN);
            String message = "• " + label + (Double.isNaN(itemScore) ? "" : String.format(" (%.0f/100)", itemScore));
            if (!action.isEmpty()) {
                message += " – " + action;
            }
            Label itemLabel = new Label(message);
            itemLabel.setWrapText(true);
            itemLabel.setStyle("-fx-text-fill: #1e293b;");
            box.getChildren().add(itemLabel);
        }

        return box;
    }

    private VBox createLifestyleCategoryCard(JSONObject category) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: rgba(14,116,144,0.15); -fx-border-radius: 10;");

        String label = category.optString("label", "Category");
        double score = category.optDouble("score", Double.NaN);
        String status = category.optString("status", "Needs attention");

        Label title = new Label(label);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        HBox scoreRow = new HBox(8);
        scoreRow.setAlignment(Pos.CENTER_LEFT);
        Label scoreLabel = new Label(Double.isNaN(score) ? "—" : String.format("%.0f/100", score));
        String color = getLifestyleScoreColor(score);
        scoreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        scoreRow.getChildren().addAll(scoreLabel, statusLabel);

        card.getChildren().addAll(title, scoreRow);

        JSONArray risks = category.optJSONArray("risks");
        if (risks != null && risks.length() > 0) {
            Label riskLabel = new Label("Needs attention: " + risks.optString(0));
            riskLabel.setWrapText(true);
            riskLabel.setStyle("-fx-text-fill: #b45309;");
            card.getChildren().add(riskLabel);
        }

        JSONArray positives = category.optJSONArray("positives");
        if (positives != null && positives.length() > 0) {
            Label positiveLabel = new Label("Strength: " + positives.optString(0));
            positiveLabel.setWrapText(true);
            positiveLabel.setStyle("-fx-text-fill: #047857;");
            card.getChildren().add(positiveLabel);
        }

        JSONArray actions = category.optJSONArray("actions");
        if (actions != null && actions.length() > 0) {
            Label actionLabel = new Label("Action: " + actions.optString(0));
            actionLabel.setWrapText(true);
            actionLabel.setStyle("-fx-text-fill: #0f172a;");
            card.getChildren().add(actionLabel);
        }
        return card;
    }

    private void updateDataQualitySummary(JSONObject dataQuality) {
        if (dataQualitySummaryBox == null) {
            return;
        }

        dataQualitySummaryBox.getChildren().clear();

        if (dataQuality == null || dataQuality.isEmpty()) {
            dataQualitySummaryBox.setVisible(false);
            dataQualitySummaryBox.setManaged(false);
            return;
        }

        dataQualitySummaryBox.setVisible(true);
        dataQualitySummaryBox.setManaged(true);

        Label title = new Label("Data Quality & Coverage");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d4ed8;");

        int missing = dataQuality.optInt("missing_features", 0);
        int total = dataQuality.optInt("total_features", 0);
        double completeness = dataQuality.optDouble("completeness", 0.0);

        ProgressBar completenessBar = new ProgressBar(Math.max(0, Math.min(1, completeness / 100.0)));
        completenessBar.setPrefWidth(Double.MAX_VALUE);
        completenessBar.setStyle("-fx-accent: #2563EB;");

        Label summary = new Label(String.format("Data completeness: %.1f%% (%d of %d lifestyle datapoints provided)",
            completeness, total - missing, total));
        summary.setWrapText(true);
        summary.setStyle("-fx-text-fill: #1e293b;");

        if (missing > 0) {
            Label hint = new Label("Tip: Add the missing details marked above to further improve precision.");
            hint.setWrapText(true);
            hint.setStyle("-fx-text-fill: #b45309;");
            dataQualitySummaryBox.getChildren().addAll(title, completenessBar, summary, hint);
        } else {
            dataQualitySummaryBox.getChildren().addAll(title, completenessBar, summary);
        }
    }

    private String getLifestyleScoreColor(double score) {
        if (Double.isNaN(score)) {
            return "#0f172a";
        }
        if (score >= 80) {
            return "#059669";
        }
        if (score >= 65) {
            return "#10B981";
        }
        if (score >= 50) {
            return "#F59E0B";
        }
        return "#DC2626";
    }

    private void maybeTriggerHealthCoachPlan(JSONObject predictions) {
        if (healthCoachPlanShown) {
            return;
        }
        if (predictions == null || predictions.isEmpty()) {
            return;
        }
        if (currentAssessment == null) {
            return;
        }

        healthCoachPlanShown = true;

        JSONObject payload = new JSONObject();
        payload.put("age", lastAge);
        payload.put("bmi", currentAssessment.getBmi());
        double score = currentAssessment.getHealthScore() != null
            ? currentAssessment.getHealthScore()
            : calculateHealthCoachScore(predictions);
        payload.put("health_score", score);
        payload.put("risk_predictions", predictions);
        if (lastFamilyHistory != null) {
            payload.put("existing_conditions", lastFamilyHistory);
        }
        if (lastSymptoms != null) {
            payload.put("symptoms", lastSymptoms);
        }

        CompletableFuture
            .supplyAsync(() -> HealthCoachApiClient.generatePlan(payload))
            .thenAccept(response -> Platform.runLater(() -> {
                if (response.optBoolean("success") && response.has("plan")) {
                    lastHealthCoachPlan = response.getJSONObject("plan");
                    displayHealthCoachSummary(lastHealthCoachPlan);
                    HealthCoachPlanView.showPlanDialog(lastHealthCoachPlan);
                    showHealthCoachNotification("A personalized 30-day health plan is ready.", NotificationHelper.NotificationType.SUCCESS);
                } else {
                    healthCoachPlanShown = false;
                    hideHealthCoachSummary();
                    showHealthCoachNotification("Health coach plan unavailable. Using standard recommendations.",
                        NotificationHelper.NotificationType.WARNING);
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    healthCoachPlanShown = false;
                    hideHealthCoachSummary();
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    showHealthCoachNotification("Health coach plan generation failed: " + message,
                        NotificationHelper.NotificationType.ERROR);
                });
                return null;
            });
    }

    private void displayHealthCoachSummary(JSONObject plan) {
        if (healthCoachSummaryBox == null) {
            return;
        }

        healthCoachSummaryBox.getChildren().clear();
        healthCoachSummaryBox.setVisible(true);
        healthCoachSummaryBox.setManaged(true);

        String primaryCondition = plan.optString("primary_condition", "general health");
        String conditionDisplay = switch (primaryCondition) {
            case "diabetes" -> "Diabetes";
            case "heart_disease" -> "Heart Disease";
            case "hypertension" -> "Hypertension";
            default -> "Whole-body Wellness";
        };

        Label title = new Label("Personalized Health Coach Plan");
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #047857;");

        Label focus = new Label("Focus: " + conditionDisplay);
        focus.setStyle("-fx-text-fill: #065f46; -fx-font-size: 13;");

        String targetSummary = "Achievable risk reduction goals tailored for you.";
        JSONObject targets = plan.optJSONObject("risk_reduction_targets");
        if (targets != null && targets.keys().hasNext()) {
            String key = targets.keys().next();
            JSONObject target = targets.optJSONObject(key);
            if (target != null) {
                double reduction = target.optDouble("target_reduction", 0.0);
                targetSummary = String.format("Goal: reduce %s risk by %.1f%% in 30 days.",
                    key.replace("_", " ").toLowerCase(), reduction);
            }
        }
        Label targetLabel = new Label(targetSummary);
        targetLabel.setWrapText(true);
        targetLabel.setStyle("-fx-text-fill: #065f46;");

        Button viewPlanButton = new Button("View Full Plan");
        viewPlanButton.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold;");
        viewPlanButton.setOnAction(e -> {
            if (lastHealthCoachPlan != null) {
                HealthCoachPlanView.showPlanDialog(lastHealthCoachPlan);
            }
        });

        Button copyPlanButton = new Button("Copy Plan");
        copyPlanButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        copyPlanButton.setOnAction(e -> {
            if (lastHealthCoachPlan != null) {
                ClipboardContent content = new ClipboardContent();
                content.putString(lastHealthCoachPlan.toString(2));
                Clipboard.getSystemClipboard().setContent(content);
                showHealthCoachNotification("Health plan copied to clipboard.", NotificationHelper.NotificationType.SUCCESS);
            }
        });

        HBox buttonRow = new HBox(10, viewPlanButton, copyPlanButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        healthCoachSummaryBox.getChildren().addAll(title, focus, targetLabel, buttonRow);
    }

    private void hideHealthCoachSummary() {
        if (healthCoachSummaryBox != null) {
            healthCoachSummaryBox.setVisible(false);
            healthCoachSummaryBox.setManaged(false);
            healthCoachSummaryBox.getChildren().clear();
        }
        lastHealthCoachPlan = null;
    }

    private void showHealthCoachNotification(String message, NotificationHelper.NotificationType type) {
        if (getScene() != null && getScene().getRoot() instanceof StackPane stackPane) {
            switch (type) {
                case SUCCESS -> NotificationHelper.showSuccessNotification(stackPane, message);
                case WARNING -> NotificationHelper.showWarningNotification(stackPane, message);
                case ERROR -> NotificationHelper.showErrorNotification(stackPane, message);
                default -> NotificationHelper.showInfoNotification(stackPane, message);
            }
        } else {
            Alert.AlertType alertType = switch (type) {
                case SUCCESS -> Alert.AlertType.INFORMATION;
                case WARNING -> Alert.AlertType.WARNING;
                case ERROR -> Alert.AlertType.ERROR;
                default -> Alert.AlertType.INFORMATION;
            };
            Alert alert = new Alert(alertType, message, ButtonType.OK);
            alert.setHeaderText(null);
            alert.show();
        }
    }

    private double calculateHealthCoachScore(JSONObject predictions) {
        double totalRisk = 0;
        int count = 0;

        for (String key : predictions.keySet()) {
            JSONObject prediction = predictions.optJSONObject(key);
            if (prediction == null) {
                continue;
            }
            double risk = prediction.optDouble("risk_percentage", 0.0);
            if (risk > 0) {
                totalRisk += risk;
                count++;
            }
        }

        double averageRisk = count > 0 ? totalRisk / count : 40.0;
        return Math.max(30.0, 100.0 - averageRisk * 0.8);
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

    private Map<String, Object> buildAdvancedExportData() {
        if (lastAdvancedResponse == null) {
            return null;
        }
        try {
            return objectMapper.readValue(lastAdvancedResponse.toString(),
                new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("Failed to prepare advanced insights for export: " + e.getMessage());
            return null;
        }
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
            lastFamilyHistory != null ? lastFamilyHistory : new ArrayList<>(),
            buildAdvancedExportData(),
            lastLifestyleInputs != null ? new HashMap<>(lastLifestyleInputs) : new HashMap<>()
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
            Map<String, Object> advancedInsights = buildAdvancedExportData();
            if (advancedInsights != null) {
                exportData.put("advanced_insights", advancedInsights);
            }
            if (lastLifestyleInputs != null && !lastLifestyleInputs.isEmpty()) {
                exportData.put("lifestyle_inputs", new HashMap<>(lastLifestyleInputs));
            }
            
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
            lastFamilyHistory != null ? lastFamilyHistory : new ArrayList<>(),
            buildAdvancedExportData(),
            lastLifestyleInputs != null ? new HashMap<>(lastLifestyleInputs) : new HashMap<>()
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


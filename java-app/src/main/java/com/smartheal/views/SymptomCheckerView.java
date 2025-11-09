package com.smartheal.views;

import com.smartheal.api.ApiClient;
import com.smartheal.dao.HistoryDAO;
import com.smartheal.models.*;
import com.smartheal.utils.FileExporter;
import com.smartheal.utils.JSONExporter;
import com.smartheal.utils.LanguageManager;
import com.smartheal.utils.NotificationHelper;
import com.smartheal.utils.ReportFormatter;
import com.smartheal.utils.UsageTracker;
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
import java.util.*;
import java.util.stream.Collectors;

public class SymptomCheckerView extends BorderPane {
    private final ApiClient apiClient;
    private final ObservableList<Symptom> allSymptoms;
    private final ObservableList<String> selectedSymptomIds;
    private final HistoryDAO historyDAO;
    private final ObjectMapper objectMapper;
    private Integer currentUserId = null; // Will be set from main app
    
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
    private VBox resultsContainer;  // Changed from ListView to VBox for stable display
    private ScrollPane resultsScrollPane;  // ScrollPane wrapping results
    private TextField searchField;
    private ScrollPane symptomScrollPane;
    private FlowPane symptomFlowPane;
    private Button analyzeButton;
    private ProgressIndicator loadingIndicator;
    private Button exportButton;
    private Button exportJsonButton;
    private Button copyButton;
    private Button clearButton;
    private ComboBox<LanguageManager.Language> languageComboBox;
    private Label panelTitleLabel;
    private Label selectedLabel;
    private TextField searchFieldRef;
    private List<SymptomCheckResult> currentResults;  // Store results for reference

    public SymptomCheckerView(ApiClient apiClient) {
        // Initialize ALL final fields FIRST - before try-catch
        // This ensures they are always initialized, preventing compilation errors
        this.apiClient = apiClient;
        this.allSymptoms = FXCollections.observableArrayList();
        this.selectedSymptomIds = FXCollections.observableArrayList();
        this.historyDAO = new HistoryDAO();
        this.objectMapper = new ObjectMapper();
        this.currentResults = new ArrayList<>();
        
        try {
            VBox headerBox = createHeader();
            setTop(headerBox);

            HBox mainContent = new HBox(15);
            mainContent.setPadding(new Insets(15));
            mainContent.setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF, #E0F2FE);");

            VBox leftPanel = createLeftPanel();
            VBox rightPanel = createRightPanel();

            mainContent.getChildren().addAll(leftPanel, rightPanel);
            HBox.setHgrow(leftPanel, Priority.SOMETIMES);
            HBox.setHgrow(rightPanel, Priority.ALWAYS);
            
            // Make panels responsive
            leftPanel.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.45));
            rightPanel.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.55));

            setCenter(mainContent);

            // Load symptoms asynchronously to avoid blocking UI
            Platform.runLater(() -> loadSymptoms());
        } catch (Exception e) {
            System.err.println("Error initializing SymptomCheckerView UI: " + e.getMessage());
            e.printStackTrace();
            // Show error message to user
            Label errorLabel = new Label("Error loading Symptom Checker: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 20;");
            setCenter(errorLabel);
        }
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.getStyleClass().add("header-section");
        headerBox.setPadding(new Insets(15, 20, 12, 20));

        // Top row with title and language selector
        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🔍 Symptom Checker");
        titleLabel.getStyleClass().add("header-title");
        
        // Language selector
        HBox languageBox = new HBox(8);
        languageBox.setAlignment(Pos.CENTER);
        
        Label langLabel = new Label("🌐 Language:");
        langLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll(LanguageManager.Language.values());
        languageComboBox.setValue(LanguageManager.getCurrentLanguage());
        languageComboBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #14B8A6; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 5 10; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold;"
        );
        
        // Set cell factory to display language names properly
        languageComboBox.setCellFactory(param -> new ListCell<LanguageManager.Language>() {
            @Override
            protected void updateItem(LanguageManager.Language language, boolean empty) {
                super.updateItem(language, empty);
                if (empty || language == null) {
                    setText(null);
                } else {
                    setText(language.getDisplayName());
                    setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                }
            }
        });
        
        // Set button cell to display selected language
        languageComboBox.setButtonCell(new ListCell<LanguageManager.Language>() {
            @Override
            protected void updateItem(LanguageManager.Language language, boolean empty) {
                super.updateItem(language, empty);
                if (empty || language == null) {
                    setText(null);
                } else {
                    setText(language.getDisplayName());
                    setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                }
            }
        });
        
        // Language change handler
        languageComboBox.setOnAction(e -> {
            LanguageManager.setLanguage(languageComboBox.getValue());
            updateUIForLanguage();
        });
        
        languageBox.getChildren().addAll(langLabel, languageComboBox);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topRow.getChildren().addAll(titleLabel, spacer, languageBox);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label descriptionLabel = new Label(
            "Select the symptoms you're experiencing to receive educational information about possible conditions"
        );
        descriptionLabel.getStyleClass().add("header-description");

        headerBox.getChildren().addAll(topRow, descriptionLabel, new DisclaimerBanner());

        return headerBox;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(12);
        leftPanel.getStyleClass().add("panel");
        
        // Store reference for language updates
        this.panelTitleLabel = new Label();
        this.selectedLabel = new Label();
        leftPanel.setMinWidth(400);
        leftPanel.setMaxWidth(600);
        VBox.setVgrow(leftPanel, Priority.ALWAYS);

        panelTitleLabel = new Label();
        panelTitleLabel.getStyleClass().add("panel-title");
        updatePanelTitle();
        
        this.searchFieldRef = new TextField();
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchField = searchFieldRef;
        searchField.setPromptText("🔍 " + LanguageManager.getUILabel("search_symptoms"));
        searchField.setPrefHeight(36);
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.setCursor(javafx.scene.Cursor.TEXT);
        searchField.setTooltip(new javafx.scene.control.Tooltip("Type to search and filter symptoms by name"));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterSymptoms(newVal));
        searchBox.getChildren().add(searchField);

        VBox selectedBox = new VBox(10);
        selectedLabel = new Label();
        selectedLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        updateSelectedLabel();
        FlowPane selectedFlowPane = new FlowPane(8, 8);
        selectedFlowPane.setHgap(8);
        selectedFlowPane.setVgap(8);

        selectedSymptomIds.addListener((javafx.collections.ListChangeListener<String>) c -> {
            selectedFlowPane.getChildren().clear();
            for (String symptomId : selectedSymptomIds) {
                Symptom symptom = allSymptoms.stream()
                    .filter(s -> s.getId().equals(symptomId))
                    .findFirst()
                    .orElse(null);
                if (symptom != null) {
                    HBox badge = createSelectedBadge(symptom);
                    selectedFlowPane.getChildren().add(badge);
                }
            }
            analyzeButton.setDisable(selectedSymptomIds.isEmpty());
            updateSelectedLabel();
        });

        selectedBox.getChildren().addAll(selectedLabel, selectedFlowPane);
        VBox.setVgrow(selectedFlowPane, Priority.NEVER);

        // Horizontal symptom layout using FlowPane
        symptomFlowPane = new FlowPane();
        symptomFlowPane.setHgap(10);
        symptomFlowPane.setVgap(10);
        symptomFlowPane.setAlignment(Pos.TOP_LEFT);
        symptomFlowPane.setPadding(new Insets(10));
        symptomFlowPane.setStyle("-fx-background-color: #FAFFFE;");
        
        symptomScrollPane = new ScrollPane(symptomFlowPane);
        symptomScrollPane.setFitToWidth(true);
        symptomScrollPane.setFitToHeight(true);
        symptomScrollPane.setStyle("-fx-background-color: transparent;");
        symptomScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        symptomScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(symptomScrollPane, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        clearButton = new Button("🗑️ " + LanguageManager.getUILabel("clear_all"));
        clearButton.getStyleClass().addAll("button", "button-warning");
        clearButton.setStyle("-fx-background-color: linear-gradient(to bottom, #F59E0B, #FBBF24); -fx-text-fill: white; -fx-font-weight: bold;");
        clearButton.setPrefHeight(40);
        clearButton.setPrefWidth(120);
        clearButton.setCursor(javafx.scene.Cursor.HAND);
        clearButton.setTooltip(new javafx.scene.control.Tooltip("Clear all selected symptoms"));
        clearButton.setOnAction(e -> clearAllSymptoms());
        
        analyzeButton = new Button("🔍 " + LanguageManager.getUILabel("analyze"));
        analyzeButton.getStyleClass().addAll("button", "button-primary");
        analyzeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        analyzeButton.setPrefHeight(40);
        analyzeButton.setPrefWidth(180);
        analyzeButton.setCursor(javafx.scene.Cursor.HAND);
        analyzeButton.setTooltip(new javafx.scene.control.Tooltip("Analyze selected symptoms using AI-powered disease prediction"));
        analyzeButton.setOnAction(e -> analyzeSymptoms());
        analyzeButton.setDisable(true);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(40, 40);
        loadingIndicator.setStyle("-fx-progress-color: linear-gradient(to right, #0F766E, #14B8A6, #22D3EE);");
        buttonBox.getChildren().addAll(clearButton, analyzeButton, loadingIndicator);

        leftPanel.getChildren().addAll(panelTitleLabel, searchBox, selectedBox, symptomScrollPane, buttonBox);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.getStyleClass().add("panel");
        rightPanel.setMinWidth(500);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label panelTitle = new Label(LanguageManager.getUILabel("analysis_results"));
        panelTitle.getStyleClass().add("panel-title");
        
        Label countLabel = new Label("(0)");
        countLabel.setFont(Font.font("System", 14));
        countLabel.setTextFill(Color.GRAY);
        this.countLabelRef = countLabel;  // Store reference
        
        // Export button
        Button exportButton = new Button("💾 Export TXT");
        exportButton.setStyle("-fx-background-color: linear-gradient(to bottom, #3B82F6, #60A5FA); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        exportButton.setTooltip(new Tooltip("Export analysis results to text file"));
        exportButton.setOnAction(e -> exportResults());
        exportButton.setDisable(true);
        
        // Export JSON button
        Button exportJsonButton = new Button("📄 Export JSON");
        exportJsonButton.setStyle("-fx-background-color: linear-gradient(to bottom, #10B981, #34D399); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        exportJsonButton.setTooltip(new Tooltip("Export analysis results to JSON file"));
        exportJsonButton.setOnAction(e -> exportResultsJSON());
        exportJsonButton.setDisable(true);
        
        // Copy button
        Button copyButton = new Button("📋 Copy");
        copyButton.setStyle("-fx-background-color: linear-gradient(to bottom, #6366F1, #818CF8); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-cursor: hand;");
        copyButton.setTooltip(new Tooltip("Copy results to clipboard"));
        copyButton.setOnAction(e -> copyResults());
        copyButton.setDisable(true);
        
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(exportButton, exportJsonButton, copyButton);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(panelTitle, countLabel, buttonBox);

        // Create stable results container using VBox instead of ListView
        resultsContainer = new VBox(15);
        resultsContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF, #E0F2FE); -fx-padding: 15;");
        resultsContainer.setAlignment(Pos.TOP_CENTER);
        resultsContainer.setFillWidth(true);
        
        // Wrap in ScrollPane for scrolling
        resultsScrollPane = new ScrollPane(resultsContainer);
        resultsScrollPane.setFitToWidth(true);
        resultsScrollPane.setFitToHeight(true);
        resultsScrollPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 0;"
        );
        resultsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        resultsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        resultsScrollPane.setVvalue(0.0);  // Start at top
        
        // Ensure ScrollPane expands to fill available space
        resultsScrollPane.setMinHeight(300);
        resultsScrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(resultsScrollPane, Priority.ALWAYS);
        
        // currentResults already initialized in constructor
        
        // Store references for export/copy methods
        this.exportButton = exportButton;
        this.exportJsonButton = exportJsonButton;
        this.copyButton = copyButton;

        rightPanel.getChildren().addAll(headerBox, resultsScrollPane);

        return rightPanel;
    }

    private HBox createSelectedBadge(Symptom symptom) {
        HBox badge = new HBox(8);
        badge.getStyleClass().add("badge");
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setStyle("-fx-background-color: linear-gradient(to right, #E0F2FE, #F0FDFA); -fx-border-color: #14B8A6; -fx-border-width: 2; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 14; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.2), 3, 0, 0, 1);");

        // Display translated symptom name in selected badge
        String displayName = LanguageManager.translateSymptom(symptom.getName());
        Label nameLabel = new Label("✓ " + displayName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.rgb(15, 118, 110));

        Button removeButton = new Button("✕");
        removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F766E; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 6;");
        removeButton.setCursor(javafx.scene.Cursor.HAND);
        removeButton.setOnAction(e -> selectedSymptomIds.remove(symptom.getId()));
        removeButton.setOnMouseEntered(e -> {
            removeButton.setTextFill(Color.rgb(239, 68, 68));
            removeButton.setStyle("-fx-background-color: rgba(239, 68, 68, 0.1); -fx-background-radius: 10; -fx-text-fill: #EF4444; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 6;");
        });
        removeButton.setOnMouseExited(e -> {
            removeButton.setTextFill(Color.rgb(15, 118, 110));
            removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #0F766E; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 6;");
        });

        badge.getChildren().addAll(nameLabel, removeButton);
        return badge;
    }

    private void loadSymptoms() {
        loadingIndicator.setVisible(true);
        analyzeButton.setDisable(true);
        
        new Thread(() -> {
            try {
                if (!apiClient.isBackendAvailable()) {
                    Platform.runLater(() -> {
                        showError("Backend Unavailable", 
                            "Cannot connect to Python backend server at http://localhost:5000.\n" +
                            "Please ensure the Python backend is running (python app.py).");
                        loadingIndicator.setVisible(false);
                    });
                    return;
                }
                
                List<Symptom> symptoms = apiClient.getSymptoms();
                Platform.runLater(() -> {
                    if (symptoms == null || symptoms.isEmpty()) {
                        showError("No Data", "No symptoms data received from backend.");
                        loadingIndicator.setVisible(false);
                        return;
                    }
                    allSymptoms.setAll(symptoms);
                    buildSymptomFlowPane();
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(selectedSymptomIds.isEmpty());
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Connection Error", 
                        "Failed to load symptoms from backend:\n" + e.getMessage() + 
                        "\n\nPlease check:\n1. Python backend server is running on port 5000\n2. Network connection is active");
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void buildSymptomFlowPane() {
        symptomFlowPane.getChildren().clear();
        
        // Add all symptoms in horizontal flow layout
        for (Symptom symptom : allSymptoms) {
            javafx.scene.Node symptomNode = createSymptomButton(symptom);
            symptomFlowPane.getChildren().add(symptomNode);
        }
    }
    
    private javafx.scene.Node createSymptomButton(Symptom symptom) {
        // Create a compact button-style symptom selector
        HBox symptomButton = new HBox(8);
        symptomButton.setAlignment(Pos.CENTER);
        symptomButton.setPadding(new Insets(8, 14, 8, 14));
        symptomButton.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand;");
        symptomButton.setMaxWidth(Double.MAX_VALUE);
        
        // Check if selected
        boolean isSelected = selectedSymptomIds.contains(symptom.getId());
        if (isSelected) {
            symptomButton.setStyle("-fx-background-color: linear-gradient(to right, #E0F2FE, #F0FDFA); -fx-border-color: #0F766E; -fx-border-width: 2.5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.2), 4, 0, 0, 2);");
        }
        
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(isSelected);
        checkBox.setCursor(javafx.scene.Cursor.HAND);
        checkBox.setStyle("-fx-font-size: 12px;");
        
        // Display translated symptom name, but keep English name for API calls
        String displayName = LanguageManager.translateSymptom(symptom.getName());
        Label nameLabel = new Label(displayName);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        nameLabel.setStyle("-fx-text-fill: #1F2937;");
        if (isSelected) {
            nameLabel.setStyle("-fx-text-fill: #0F766E; -fx-font-weight: bold;");
        }
        
        symptomButton.getChildren().addAll(checkBox, nameLabel);
        
        // Click handlers
        checkBox.setOnAction(e -> {
            boolean wasSelected = selectedSymptomIds.contains(symptom.getId());
            if (checkBox.isSelected() && !wasSelected) {
                selectedSymptomIds.add(symptom.getId());
                updateSymptomButtonStyle(symptomButton, nameLabel, true);
            } else if (!checkBox.isSelected() && wasSelected) {
                selectedSymptomIds.remove(symptom.getId());
                updateSymptomButtonStyle(symptomButton, nameLabel, false);
            }
        });
        
        symptomButton.setOnMouseClicked(e -> {
            if (e.getTarget() != checkBox && e.getTarget() != checkBox.getGraphic()) {
                checkBox.setSelected(!checkBox.isSelected());
            }
        });
        
        symptomButton.setOnMouseEntered(e -> {
            if (!selectedSymptomIds.contains(symptom.getId())) {
                symptomButton.setStyle("-fx-background-color: linear-gradient(to right, #F0FDFA, #F0F9FF); -fx-border-color: #14B8A6; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.1), 3, 0, 0, 1);");
            }
        });
        
        symptomButton.setOnMouseExited(e -> {
            if (!selectedSymptomIds.contains(symptom.getId())) {
                symptomButton.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: none;");
            }
        });
        
        return symptomButton;
    }
    
    private void updateSymptomButtonStyle(HBox button, Label label, boolean selected) {
        if (selected) {
            button.setStyle("-fx-background-color: linear-gradient(to right, #E0F2FE, #F0FDFA); -fx-border-color: #0F766E; -fx-border-width: 2.5; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.2), 4, 0, 0, 2);");
            label.setStyle("-fx-text-fill: #0F766E; -fx-font-weight: bold;");
        } else {
            button.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 2; -fx-background-radius: 20; -fx-border-radius: 20; -fx-cursor: hand; -fx-effect: none;");
            label.setStyle("-fx-text-fill: #1F2937;");
        }
    }


    private void filterSymptoms(String searchTerm) {
        symptomFlowPane.getChildren().clear();
        
        List<Symptom> symptomsToShow = allSymptoms;
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            // Search in both English and translated names
            symptomsToShow = allSymptoms.stream()
                .filter(s -> {
                    String englishName = s.getName().toLowerCase();
                    String translatedName = LanguageManager.translateSymptom(s.getName()).toLowerCase();
                    return englishName.contains(lowerSearch) || translatedName.contains(lowerSearch);
                })
                .collect(Collectors.toList());
        }
        
        for (Symptom symptom : symptomsToShow) {
            javafx.scene.Node symptomNode = createSymptomButton(symptom);
            symptomFlowPane.getChildren().add(symptomNode);
        }
    }
    
    private void updateUIForLanguage() {
        // Update all UI labels
        updatePanelTitle();
        updateSelectedLabel();
        
        // Update search field placeholder
        if (searchField != null) {
            searchField.setPromptText("🔍 " + LanguageManager.getUILabel("search_symptoms"));
        }
        
        // Update buttons
        if (analyzeButton != null) {
            analyzeButton.setText("🔍 " + LanguageManager.getUILabel("analyze"));
        }
        
        if (clearButton != null) {
            clearButton.setText("🗑️ " + LanguageManager.getUILabel("clear_all"));
        }
        
        // Rebuild symptom buttons with new language
        buildSymptomFlowPane();
        
        // Rebuild selected symptoms badges
        Platform.runLater(() -> {
            // Force refresh of selected symptoms display
            if (!selectedSymptomIds.isEmpty()) {
                List<String> ids = new ArrayList<>(selectedSymptomIds);
                selectedSymptomIds.clear();
                selectedSymptomIds.addAll(ids);
            }
        });
        
        // Refresh results display if there are results
        if (resultsContainer != null && !currentResults.isEmpty()) {
            resultsContainer.getChildren().clear();
            for (SymptomCheckResult result : currentResults) {
                javafx.scene.Node resultCard = createResultCard(result);
                resultsContainer.getChildren().add(resultCard);
            }
        }
    }
    
    private void updatePanelTitle() {
        if (panelTitleLabel != null) {
            panelTitleLabel.setText(LanguageManager.getUILabel("select_symptoms"));
        }
    }
    
    private void updateSelectedLabel() {
        if (selectedLabel != null) {
            String labelText = LanguageManager.getUILabel("selected_symptoms");
            selectedLabel.setText(labelText + " (" + selectedSymptomIds.size() + "):");
        }
    }

    private void analyzeSymptoms() {
        if (selectedSymptomIds.isEmpty()) {
            showError("No Symptoms Selected", "Please select at least one symptom to analyze");
            return;
        }
        
        if (selectedSymptomIds.size() > 20) {
            showError("Too Many Symptoms", "Please select no more than 20 symptoms for analysis.");
            return;
        }

        analyzeButton.setDisable(true);
        loadingIndicator.setVisible(true);
        resultsContainer.getChildren().clear();
        currentResults.clear();
        
        NotificationHelper.showInfoNotification(
            (StackPane) getScene().getRoot(),
            "Analyzing " + selectedSymptomIds.size() + " symptom(s)... Please wait."
        );

        new Thread(() -> {
            try {
                System.out.println("Sending symptom IDs to backend: " + selectedSymptomIds);
                List<SymptomCheckResult> results = apiClient.checkSymptoms(new ArrayList<>(selectedSymptomIds));
                System.out.println("Received results from backend: " + (results != null ? results.size() : "null") + " items");
                
                Platform.runLater(() -> {
                    if (results == null || results.isEmpty()) {
                        System.out.println("WARNING: No results returned from backend");
                        resultsContainer.getChildren().clear();
                        currentResults.clear();
                        analyzeButton.setDisable(false);
                        loadingIndicator.setVisible(false);
                        updateResultsCount(0);
                        NotificationHelper.showInfoNotification(
                            (StackPane) getScene().getRoot(),
                            "No matching conditions found. Please try different symptoms."
                        );
                    } else {
                        System.out.println("Processing " + results.size() + " results for display");
                        
                        // Clear previous results
                        resultsContainer.getChildren().clear();
                        currentResults = new ArrayList<>(results);
                        
                        // Add result cards to container
                        for (SymptomCheckResult result : results) {
                            javafx.scene.Node resultCard = createResultCard(result);
                            resultsContainer.getChildren().add(resultCard);
                        }
                        
                        // Update results count and enable buttons
                        updateResultsCount(results.size());
                        
                        // Scroll to top
                        resultsScrollPane.setVvalue(0.0);
                        
                        System.out.println("Results set: " + results.size() + " items added to results container");
                        
                        UsageTracker.incrementAnalysesDone();
                        UsageTracker.incrementSymptomsChecked();
                        
                        // Save to history if user is logged in
                        if (currentUserId != null) {
                            try {
                                List<String> symptomNames = selectedSymptomIds.stream()
                                    .map(id -> allSymptoms.stream()
                                        .filter(s -> s.getId().equals(id))
                                        .findFirst()
                                        .map(Symptom::getName)
                                        .orElse(id))
                                    .collect(Collectors.toList());
                                
                                String symptomsStr = String.join(", ", symptomNames);
                                String conditionsStr = results.stream()
                                    .map(r -> r.getDisease().getName())
                                    .collect(Collectors.joining(", "));
                                
                                String analysisJson = objectMapper.writeValueAsString(results);
                                
                                historyDAO.saveSymptomHistory(currentUserId, symptomsStr, conditionsStr, analysisJson);
                            } catch (Exception e) {
                                System.err.println("Failed to save symptom history: " + e.getMessage());
                            }
                        }
                        
                        NotificationHelper.showSuccessNotification(
                            (StackPane) getScene().getRoot(),
                            "Analysis complete! Found " + results.size() + " possible condition(s)."
                        );
                    }
                    analyzeButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (IOException e) {
                System.err.println("IO Error during symptom analysis: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    String errorMsg = "Failed to analyze symptoms:\n" + e.getMessage();
                    
                    // Check if it's a connection error
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout") ||
                        e.getMessage().contains("Cannot connect")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Check: http://localhost:5000/api/symptoms in browser\n" +
                                   "3. Verify Python backend server started successfully (python app.py)";
                    }
                    
                    showError("Analysis Error", errorMsg);
                    analyzeButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                System.err.println("Unexpected error during symptom analysis: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Unexpected Error", 
                        "An unexpected error occurred:\n" + e.getMessage() + 
                        "\n\nPlease check the console for details and try again.");
                    analyzeButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void clearAllSymptoms() {
        selectedSymptomIds.clear();
        
        // Clear all checkboxes and update styles
        for (javafx.scene.Node node : symptomFlowPane.getChildren()) {
            if (node instanceof HBox) {
                HBox symptomButton = (HBox) node;
                for (javafx.scene.Node child : symptomButton.getChildren()) {
                    if (child instanceof CheckBox) {
                        ((CheckBox) child).setSelected(false);
                    } else if (child instanceof Label) {
                        Label label = (Label) child;
                        if (label.getText() != null && !label.getText().startsWith("✓")) {
                            updateSymptomButtonStyle(symptomButton, label, false);
                        }
                    }
                }
            }
        }
        
        // Rebuild to update all button styles
        buildSymptomFlowPane();
        
        // Clear results
        resultsContainer.getChildren().clear();
        currentResults.clear();
        updateResultsCount(0);
        analyzeButton.setDisable(true);
        
        NotificationHelper.showInfoNotification(
            (StackPane) getScene().getRoot(),
            "All symptoms cleared. Ready for new selection."
        );
    }

    private void exportResults() {
        if (currentResults == null || currentResults.isEmpty()) {
            showError("No Results", "No analysis results to export.");
            return;
        }

        List<String> symptomNames = selectedSymptomIds.stream()
            .map(id -> allSymptoms.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .map(Symptom::getName)
                .orElse(id))
            .collect(Collectors.toList());

        String report = ReportFormatter.formatSymptomAnalysis(
            new ArrayList<>(currentResults),
            symptomNames
        );

        FileExporter.exportToFile(
            report,
            "symptom_analysis",
            "Export Symptom Analysis Report",
            (Stage) getScene().getWindow()
        );
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "Report exported successfully!"
        );
    }

    private void exportResultsJSON() {
        if (currentResults == null || currentResults.isEmpty()) {
            showError("No Results", "No analysis results to export.");
            return;
        }

        Map<String, Object> exportData = new java.util.HashMap<>();
        exportData.put("timestamp", new java.util.Date().toString());
        exportData.put("selectedSymptoms", selectedSymptomIds.stream()
            .map(id -> allSymptoms.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .map(Symptom::getName)
                .orElse(id))
            .collect(Collectors.toList()));
        exportData.put("results", currentResults);

        JSONExporter.exportToJSON(
            exportData,
            "symptom_analysis",
            "Export Symptom Analysis as JSON",
            (Stage) getScene().getWindow()
        );
        
        NotificationHelper.showSuccessNotification(
            (StackPane) getScene().getRoot(),
            "JSON exported successfully!"
        );
    }

    private void copyResults() {
        if (currentResults == null || currentResults.isEmpty()) {
            showError("No Results", "No analysis results to copy.");
            return;
        }

        List<String> symptomNames = selectedSymptomIds.stream()
            .map(id -> allSymptoms.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .map(Symptom::getName)
                .orElse(id))
            .collect(Collectors.toList());

        String report = ReportFormatter.formatSymptomAnalysis(
            new ArrayList<>(currentResults),
            symptomNames
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

    private Label countLabelRef;  // Reference to count label
    
    private void updateResultsCount(int count) {
        if (countLabelRef != null) {
            countLabelRef.setText("(" + count + ")");
        }
        boolean hasResults = count > 0;
        if (exportButton != null) exportButton.setDisable(!hasResults);
        if (exportJsonButton != null) exportJsonButton.setDisable(!hasResults);
        if (copyButton != null) copyButton.setDisable(!hasResults);
    }
    
    private javafx.scene.Node createResultCard(SymptomCheckResult result) {
        return createResultCardInternal(result);
    }
    
    private static javafx.scene.Node createResultCardInternal(SymptomCheckResult result) {
        try {
            Disease disease = result.getDisease();
            if (disease == null) {
                Label errorLabel = new Label("Error: Disease information is missing");
                errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 14px; -fx-padding: 20;");
                return errorLabel;
            }

            String severity = disease.getSeverity() != null ? disease.getSeverity() : "moderate";
            String borderColor = getBorderColorForSeverity(severity);
            VBox card = new VBox(18);
            card.getStyleClass().add("card");
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, rgba(255,255,255,0.92), rgba(240,249,255,0.95));" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 3;" +
                "-fx-padding: 24;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,118,110,0.18), 20, 0, 0, 10);" +
                "-fx-min-width: 520;" +
                "-fx-max-width: 900;" +
                "-fx-pref-width: 760;"
            );

            Label nameLabel = new Label(disease.getName() != null ? disease.getName() : "Unknown Condition");
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            nameLabel.setStyle("-fx-text-fill: #0f172a;");
            nameLabel.setWrapText(true);

            HBox badgeRow = new HBox(12);
            badgeRow.setAlignment(Pos.CENTER_LEFT);

            Label severityBadge = new Label(severity.toUpperCase() + " SEVERITY");
            severityBadge.setStyle(getSeverityBadgeStyle(severity) + " -fx-padding: 6 12; -fx-font-size: 13px; -fx-font-weight: bold;");

            Label matchLabel = new Label(String.format("%.1f%% Model match", result.getMatchPercentage()));
            matchLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            matchLabel.setTextFill(getSeverityColor(severity));

            badgeRow.getChildren().addAll(severityBadge, matchLabel);

            ProgressBar probabilityBar = new ProgressBar(Math.min(Math.max(result.getMatchPercentage() / 100.0, 0), 1));
            probabilityBar.setPrefHeight(10);
            probabilityBar.setStyle("-fx-accent: " + getSeverityProgressColor(severity) + "; -fx-background-color: #e5e7eb;");

            SymptomTriage triage = result.getTriage();
            if (triage != null && triage.getLevel() != null) {
                VBox triageBox = new VBox(6);
                String color = triage.getColor() != null ? triage.getColor() : "#0ea5e9";
                triageBox.setStyle(
                    "-fx-background-color: " + color + "22;" +
                    "-fx-border-color: " + color + ";" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 12;"
                );

                Label triageTitle = new Label("Triage Advice: " + triage.getLevel().toUpperCase());
                triageTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
                triageTitle.setTextFill(Color.web(color));

                Label triageMessage = new Label(triage.getMessage());
                triageMessage.setWrapText(true);
                triageMessage.setStyle("-fx-text-fill: #0f172a;");

                if (triage.getSpecialist() != null && !triage.getSpecialist().isEmpty()) {
                    Label specialistLabel = new Label("Recommended specialist: " + triage.getSpecialist());
                    specialistLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px;");
                    triageBox.getChildren().addAll(triageTitle, triageMessage, specialistLabel);
                } else {
                    triageBox.getChildren().addAll(triageTitle, triageMessage);
                }
                card.getChildren().add(triageBox);
            }

            SymptomConfidence confidence = result.getConfidence();
            if (confidence != null) {
                VBox confidenceBox = new VBox(6);
                confidenceBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5f5; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12;");

                Label confidenceLabel = new Label(
                    String.format("Confidence: %.1f%% (%s)", 
                        confidence.getScore() != null ? confidence.getScore() : result.getMatchPercentage(),
                        confidence.getLevel() != null ? confidence.getLevel() : "N/A")
                );
                confidenceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                confidenceLabel.setStyle("-fx-text-fill: #0f172a;");

                ProgressBar confidenceBar = new ProgressBar(
                    Math.min(Math.max((confidence.getScore() != null ? confidence.getScore() : result.getMatchPercentage()) / 100.0, 0), 1)
                );
                confidenceBar.setPrefHeight(8);
                confidenceBar.setStyle("-fx-accent: #0ea5e9;");

                Label coverageLabel = new Label(
                    String.format("Symptom overlap: %.1f%%",
                        confidence.getSymptomCoverage() != null ? confidence.getSymptomCoverage() : result.getSymptomCoverage())
                );
                coverageLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

                if (confidence.getExplanation() != null) {
                    Label explanation = new Label(confidence.getExplanation());
                    explanation.setWrapText(true);
                    explanation.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
                    confidenceBox.getChildren().addAll(confidenceLabel, confidenceBar, coverageLabel, explanation);
                } else {
                    confidenceBox.getChildren().addAll(confidenceLabel, confidenceBar, coverageLabel);
                }

                card.getChildren().add(confidenceBox);
            }

            Label descLabel = new Label(
                LanguageManager.getUILabel("description") + ": " +
                (disease.getDescription() != null ? disease.getDescription() : "No description available.")
            );
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14px;");
            descLabel.setLineSpacing(3);

            card.getChildren().addAll(nameLabel, badgeRow, probabilityBar, descLabel);

            if (result.getRedFlags() != null && !result.getRedFlags().isEmpty()) {
                VBox warningBox = new VBox(6);
                warningBox.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12;");
                Label warningTitle = new Label("⚠️ Critical symptoms reported");
                warningTitle.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
                FlowPane warningFlow = buildTagFlow(result.getRedFlags(), "#fee2e2", "#b91c1c");
                warningBox.getChildren().addAll(warningTitle, warningFlow);
                card.getChildren().add(warningBox);
            }

            List<String> matchedTranslated = translateSymptomsList(result.getMatchedSymptoms());
            card.getChildren().add(buildTagSection("Matched symptoms", matchedTranslated, "#dbeafe", "#0f766e"));
            if (result.getMissingSymptoms() != null && !result.getMissingSymptoms().isEmpty()) {
                List<String> missingTranslated = translateSymptomsList(result.getMissingSymptoms());
                card.getChildren().add(buildTagSection("Typical symptoms not reported", missingTranslated, "#fef9c3", "#f59e0b"));
            }
            if (result.getCriticalSymptomsMissing() != null && !result.getCriticalSymptomsMissing().isEmpty()) {
                List<String> criticalTranslated = translateSymptomsList(result.getCriticalSymptomsMissing());
                card.getChildren().add(buildTagSection("Key diagnostic clues to monitor", criticalTranslated, "#fee2e2", "#dc2626"));
            }

            card.getChildren().add(buildBulletSection(LanguageManager.getUILabel("common_treatments"), disease.getTreatments(), "#0f172a"));
            card.getChildren().add(buildBulletSection("Recommended tests & investigations", result.getRecommendedTests(), "#0f172a"));
            card.getChildren().add(buildBulletSection("Lifestyle & self-care guidance", result.getLifestyleAdvice(), "#0f172a"));

            if (result.getMonitoringTips() != null && !result.getMonitoringTips().isEmpty()) {
                card.getChildren().add(buildBulletSection("Home monitoring tips", result.getMonitoringTips(), "#0f172a"));
            }

            if (result.getRiskFactors() != null && !result.getRiskFactors().isEmpty()) {
                card.getChildren().add(buildBulletSection("Common risk factors", result.getRiskFactors(), "#0f172a"));
            }

            card.getChildren().add(buildBulletSection(LanguageManager.getUILabel("when_to_seek_help"), 
                List.of(disease.getWhenToSeekHelp() != null ? disease.getWhenToSeekHelp() : "Consult a qualified healthcare provider."), "#dc2626"));

            if (result.getPrecautions() != null && !result.getPrecautions().isEmpty()) {
                card.getChildren().add(buildBulletSection("Precautions & follow-up", result.getPrecautions(), "#0f172a"));
            }

            if (result.getSimilarConditions() != null && !result.getSimilarConditions().isEmpty()) {
                FlowPane similarFlow = new FlowPane(8, 8);
                similarFlow.setPrefWrapLength(680);
                for (SimilarCondition sc : result.getSimilarConditions()) {
                    if (sc.getName() != null) {
                        String text = sc.getName();
                        if (sc.getMatchPercentage() != null) {
                            text += String.format(" (%.1f%%)", sc.getMatchPercentage());
                        }
                        Label chip = new Label(text);
                        chip.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-padding: 6 12; -fx-background-radius: 10; -fx-font-size: 12px;");
                        similarFlow.getChildren().add(chip);
                    }
                }
                VBox similarBox = new VBox(6, new Label("Other possibilities to discuss:"), similarFlow);
                ((Label) similarBox.getChildren().get(0)).setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
                card.getChildren().add(similarBox);
            }

            return card;
        } catch (Exception e) {
            System.err.println("Error creating result card: " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Error displaying result: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 14px; -fx-padding: 20;");
            return errorLabel;
        }
    }

    private static FlowPane buildTagFlow(List<String> items, String background, String textColor) {
        FlowPane flow = new FlowPane(8, 8);
        flow.setPrefWrapLength(680);
        if (items != null) {
            for (String item : items) {
                if (item != null && !item.isEmpty()) {
                    Label badge = new Label(item);
                    badge.setStyle(
                        "-fx-background-color: " + background + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: " + textColor + "66;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
                    );
                    flow.getChildren().add(badge);
                }
            }
        }
        return flow;
    }

    private static VBox buildTagSection(String title, List<String> items, String background, String textColor) {
        VBox container = new VBox(6);
        Label sectionLabel = new Label(title + ":");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
        FlowPane flow = buildTagFlow(items, background, textColor);
        if (flow.getChildren().isEmpty()) {
            Label none = new Label("None reported");
            none.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
            container.getChildren().addAll(sectionLabel, none);
        } else {
            container.getChildren().addAll(sectionLabel, flow);
        }
        return container;
    }

    private static VBox buildBulletSection(String title, List<String> items, String textColor) {
        VBox section = new VBox(6);
        Label sectionTitle = new Label(title + ":");
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");
        section.getChildren().add(sectionTitle);

        if (items == null || items.isEmpty()) {
            Label none = new Label("No data available.");
            none.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
            section.getChildren().add(none);
        } else {
            VBox list = new VBox(4);
            for (String item : items) {
                if (item != null && !item.isEmpty()) {
                    Label entry = new Label("• " + item);
                    entry.setWrapText(true);
                    entry.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px;");
                    list.getChildren().add(entry);
                }
            }
            section.getChildren().add(list);
        }
        return section;
    }

    private static List<String> translateSymptomsList(List<String> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return Collections.emptyList();
        }
        return symptoms.stream()
            .filter(item -> item != null && !item.isBlank())
            .map(LanguageManager::translateSymptom)
            .collect(Collectors.toList());
    }
    
    // Static helper methods for styling
    private static Color getSeverityColor(String severity) {
        switch (severity.toLowerCase()) {
            case "low": return Color.rgb(34, 197, 94);
            case "moderate": return Color.rgb(245, 158, 11);
            case "high": return Color.rgb(239, 68, 68);
            default: return Color.GRAY;
        }
    }
    
    private static String getSeverityBadgeStyle(String severity) {
        switch (severity.toLowerCase()) {
            case "low": return "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 4 10; -fx-background-radius: 12;";
            case "moderate": return "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 4 10; -fx-background-radius: 12;";
            case "high": return "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 4 10; -fx-background-radius: 12;";
            default: return "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-padding: 4 10; -fx-background-radius: 12;";
        }
    }
    
    private static String getSeverityProgressColor(String severity) {
        switch (severity.toLowerCase()) {
            case "low": return "#22C55E";
            case "moderate": return "#F59E0B";
            case "high": return "#EF4444";
            default: return "#6B7280";
        }
    }
    
    private static String getCardColor(String severity) {
        switch (severity.toLowerCase()) {
            case "low": return "#F0FDF4";
            case "moderate": return "#FFFBEB";
            case "high": return "#FEF2F2";
            default: return "#F0F9FF";
        }
    }
    
    private static String getGradientStyle(String severity) {
        // Return colorful gradient backgrounds instead of solid colors
        switch (severity.toLowerCase()) {
            case "low": 
                return "linear-gradient(to bottom right, #ECFDF5, #D1FAE5, #A7F3D0)";  // Green gradient
            case "moderate": 
                return "linear-gradient(to bottom right, #FFFBEB, #FEF3C7, #FDE68A)";  // Yellow/amber gradient
            case "high": 
                return "linear-gradient(to bottom right, #FEF2F2, #FEE2E2, #FECACA)";  // Red gradient
            default: 
                return "linear-gradient(to bottom right, #F0F9FF, #E0F2FE, #BAE6FD)";  // Blue gradient
        }
    }
    
    private static String getBorderColorForSeverity(String severity) {
        switch (severity.toLowerCase()) {
            case "low": return "#22C55E";
            case "moderate": return "#F59E0B";
            case "high": return "#EF4444";
            default: return "#E5E7EB";
        }
    }
}


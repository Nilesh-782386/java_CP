package com.smartheal.views;

import com.smartheal.components.QuickStatsPanel;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class DashboardView extends BorderPane {
    private QuickStatsPanel statsPanel;
    private Runnable onSymptomCheckerClick;
    private Runnable onHealthChatbotClick;
    private Runnable onCostEstimatorClick;
    private Runnable onReportAnalyzerClick;
    private Runnable onRiskAssessmentClick;
    
    // Store card references to update click handlers
    private VBox symptomCard;
    private VBox chatbotCard;
    private VBox costEstimatorCard;
    private VBox reportAnalyzerCard;
    private VBox riskAssessmentCard;

    public DashboardView() {
        createDashboard();
    }

    private void createDashboard() {
        // Set background gradient
        setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF 0%, #E0F2FE 50%, #F0F9FF 100%);");

        // Header
        VBox headerBox = createHeader();
        setTop(headerBox);

        // Center content with module cards
        VBox centerBox = createCenterContent();
        setCenter(centerBox);

        // Stats panel at bottom
        statsPanel = new QuickStatsPanel();
        com.smartheal.utils.UsageTracker.setStatsPanel(statsPanel);
        VBox bottomBox = new VBox(statsPanel);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5);");
        setBottom(bottomBox);
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(30, 30, 20, 30));
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #0F766E, #14B8A6, #22D3EE); -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.3), 10, 0, 0, 4);");

        Label titleLabel = new Label("🏥 SMART Health Guide+");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        Label subtitleLabel = new Label("AI-Powered Medical Assistant & Health Advisor");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.rgb(255, 255, 255, 0.95));
        subtitleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");

        Label versionLabel = new Label("Version 2.0 - Dashboard Edition");
        versionLabel.setFont(Font.font("System", 12));
        versionLabel.setTextFill(Color.rgb(255, 255, 255, 0.85));

        headerBox.getChildren().addAll(titleLabel, subtitleLabel, versionLabel);
        headerBox.setAlignment(Pos.CENTER);

        return headerBox;
    }

    private VBox createCenterContent() {
        VBox centerBox = new VBox(20);
        centerBox.setPadding(new Insets(40));
        centerBox.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome! Choose a module to get started");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        welcomeLabel.setTextFill(Color.rgb(31, 41, 55));
        welcomeLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        // Create grid of module cards
        GridPane gridPane = new GridPane();
        gridPane.setHgap(25);
        gridPane.setVgap(25);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(20));

        // Symptom Checker Card
        symptomCard = createModuleCard(
            "🩺",
            "Symptom Checker",
            "Analyze your symptoms to identify possible health conditions using AI-powered disease prediction",
            "#0F766E",
            "SYMPTOM_CHECKER"
        );

        // Health Chatbot Card
        chatbotCard = createModuleCard(
            "💬",
            "Health Chatbot",
            "Ask questions about health and wellness. Get instant educational answers from our ML-powered knowledge base",
            "#14B8A6",
            "CHATBOT"
        );

        // Cost Estimator Card
        costEstimatorCard = createModuleCard(
            "💰",
            "Cost Estimator",
            "Estimate treatment costs for different hospital types using ML-based cost forecasting",
            "#3B82F6",
            "COST_ESTIMATOR"
        );

        // Report Analyzer Card
        reportAnalyzerCard = createModuleCard(
            "📊",
            "Report Analyzer",
            "Analyze blood test reports with AI insights. Get detailed analysis and recommendations",
            "#8B5CF6",
            "REPORT_ANALYZER"
        );

        // Risk Assessment Card
        riskAssessmentCard = createModuleCard(
            "⚠️",
            "Risk Assessment",
            "Calculate your personal disease risks using AI. Get predictions for diabetes, heart disease, and hypertension",
            "#F59E0B",
            "RISK_ASSESSMENT"
        );

        // Add cards to grid (2 rows, 3 columns with 5 items)
        gridPane.add(symptomCard, 0, 0);
        gridPane.add(chatbotCard, 1, 0);
        gridPane.add(costEstimatorCard, 2, 0);
        gridPane.add(reportAnalyzerCard, 0, 1);
        gridPane.add(riskAssessmentCard, 1, 1);

        // Set column constraints for equal width
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.33);
        gridPane.getColumnConstraints().addAll(col1, col2, col3);

        centerBox.getChildren().addAll(welcomeLabel, gridPane);
        VBox.setVgrow(gridPane, Priority.ALWAYS);

        return centerBox;
    }

    private VBox createModuleCard(String icon, String title, String description, String color, String cardType) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(320);
        card.setPrefHeight(280);
        card.setMaxWidth(350);
        card.setMaxHeight(320);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: " + color + "; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5); " +
            "-fx-cursor: hand;"
        );
        
        // Make card fully clickable
        card.setPickOnBounds(true);
        card.setMouseTransparent(false);

        // Icon - make mouse transparent so clicks go to card
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 64));
        iconLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        iconLabel.setMouseTransparent(true);

        // Title - make mouse transparent so clicks go to card
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(color));
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        titleLabel.setMouseTransparent(true);

        // Description - make mouse transparent so clicks go to card
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", 13));
        descLabel.setTextFill(Color.rgb(75, 85, 99));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(280);
        descLabel.setMouseTransparent(true);

        // Hover effect (without scale to avoid click issues)
        card.setOnMouseEntered(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(200), card);
            fade.setFromValue(1.0);
            fade.setToValue(0.95);
            fade.play();
            
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FAFFFE, white); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-width: 4; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, " + color + "80, 20, 0, 0, 8); " +
                "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(200), card);
            fade.setFromValue(0.95);
            fade.setToValue(1.0);
            fade.play();
            
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5); " +
                "-fx-cursor: hand;"
            );
        });

        // Set up click handler based on card type
        setupCardClickHandler(card, cardType, title);
        
        // Ensure card receives mouse events
        card.setPickOnBounds(true);
        card.setMouseTransparent(false);
        card.setFocusTraversable(false);
        
        // Make sure card is always interactive
        card.setOnDragDetected(e -> card.startFullDrag());

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        return card;
    }

    private void setupCardClickHandler(VBox card, String cardType, String title) {
        // Clear any existing handlers first
        card.setOnMousePressed(null);
        card.setOnMouseClicked(null);
        
        // Get the appropriate callback
        Runnable callback = null;
        switch (cardType) {
            case "SYMPTOM_CHECKER":
                callback = onSymptomCheckerClick;
                break;
            case "CHATBOT":
                callback = onHealthChatbotClick;
                break;
            case "COST_ESTIMATOR":
                callback = onCostEstimatorClick;
                break;
            case "REPORT_ANALYZER":
                callback = onReportAnalyzerClick;
                break;
            case "RISK_ASSESSMENT":
                callback = onRiskAssessmentClick;
                break;
        }
        
        final Runnable finalCallback = callback; // Make effectively final for lambda
        
        card.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                System.out.println("[DEBUG] " + title + " card MOUSE PRESSED! Type: " + cardType + ", Callback: " + (finalCallback != null ? "SET" : "NULL"));
                // Don't consume here - let it propagate to clicked
            }
        });
        
        card.setOnMouseClicked(e -> {
            System.out.println("[DEBUG] " + title + " card MOUSE CLICKED! Type: " + cardType);
            System.out.println("[DEBUG] Click count: " + e.getClickCount() + ", Button: " + e.getButton());
            e.consume(); // Consume to prevent double-firing
            
            if (finalCallback != null) {
                System.out.println("[DEBUG] Executing callback for " + title);
                try {
                    finalCallback.run();
                } catch (Exception ex) {
                    System.err.println("[ERROR] Exception executing callback: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                System.err.println("[WARNING] Callback is NULL for " + title + " (type: " + cardType + ")");
                System.err.println("[WARNING] Current callback state:");
                System.err.println("  - onSymptomCheckerClick: " + (onSymptomCheckerClick != null));
                System.err.println("  - onHealthChatbotClick: " + (onHealthChatbotClick != null));
                System.err.println("  - onCostEstimatorClick: " + (onCostEstimatorClick != null));
                System.err.println("  - onReportAnalyzerClick: " + (onReportAnalyzerClick != null));
                System.err.println("  - onRiskAssessmentClick: " + (onRiskAssessmentClick != null));
            }
        });
        
        // Also handle mouse released as backup
        card.setOnMouseReleased(e -> {
            if (e.isStillSincePress() && e.getButton().toString().equals("PRIMARY")) {
                System.out.println("[DEBUG] " + title + " card MOUSE RELEASED (click detected)");
            }
        });
    }
    
    // Setters for navigation callbacks
    public void setOnSymptomCheckerClick(Runnable callback) {
        System.out.println("Setting Symptom Checker callback: " + (callback != null ? "OK" : "NULL"));
        this.onSymptomCheckerClick = callback;
        // Update card click handler immediately
        if (symptomCard != null) {
            setupCardClickHandler(symptomCard, "SYMPTOM_CHECKER", "Symptom Checker");
        }
    }

    public void setOnHealthChatbotClick(Runnable callback) {
        System.out.println("Setting Health Chatbot callback: " + (callback != null ? "OK" : "NULL"));
        this.onHealthChatbotClick = callback;
        if (chatbotCard != null) {
            setupCardClickHandler(chatbotCard, "CHATBOT", "Health Chatbot");
        }
    }

    public void setOnCostEstimatorClick(Runnable callback) {
        System.out.println("Setting Cost Estimator callback: " + (callback != null ? "OK" : "NULL"));
        this.onCostEstimatorClick = callback;
        if (costEstimatorCard != null) {
            setupCardClickHandler(costEstimatorCard, "COST_ESTIMATOR", "Cost Estimator");
        }
    }

    public void setOnReportAnalyzerClick(Runnable callback) {
        System.out.println("Setting Report Analyzer callback: " + (callback != null ? "OK" : "NULL"));
        this.onReportAnalyzerClick = callback;
        if (reportAnalyzerCard != null) {
            setupCardClickHandler(reportAnalyzerCard, "REPORT_ANALYZER", "Report Analyzer");
        }
    }

    public void setOnRiskAssessmentClick(Runnable callback) {
        System.out.println("Setting Risk Assessment callback: " + (callback != null ? "OK" : "NULL"));
        this.onRiskAssessmentClick = callback;
        if (riskAssessmentCard != null) {
            setupCardClickHandler(riskAssessmentCard, "RISK_ASSESSMENT", "Risk Assessment");
        }
    }

    public QuickStatsPanel getStatsPanel() {
        return statsPanel;
    }
}


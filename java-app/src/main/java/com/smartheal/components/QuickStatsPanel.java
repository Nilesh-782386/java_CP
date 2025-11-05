package com.smartheal.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class QuickStatsPanel extends HBox {
    private Label symptomsCheckedLabel;
    private Label analysesDoneLabel;
    private Label reportsAnalyzedLabel;
    private Label costsEstimatedLabel;
    
    private int symptomsChecked = 0;
    private int analysesDone = 0;
    private int reportsAnalyzed = 0;
    private int costsEstimated = 0;

    public QuickStatsPanel() {
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 12, 8, 12));
        setStyle("-fx-background-color: linear-gradient(to right, rgba(15, 118, 110, 0.05), rgba(20, 184, 166, 0.05)); -fx-background-radius: 8;");
        setPrefHeight(60);

        // Symptom Checker Stats
        VBox symptomBox = createStatBox("🔍", "Symptoms Checked", "0", "#0F766E");
        symptomsCheckedLabel = (Label) symptomBox.getChildren().get(1);
        
        // Analysis Stats
        VBox analysisBox = createStatBox("📊", "Analyses Done", "0", "#14B8A6");
        analysesDoneLabel = (Label) analysisBox.getChildren().get(1);
        
        // Report Stats
        VBox reportBox = createStatBox("📋", "Reports Analyzed", "0", "#22D3EE");
        reportsAnalyzedLabel = (Label) reportBox.getChildren().get(1);
        
        // Cost Stats
        VBox costBox = createStatBox("💰", "Costs Estimated", "0", "#3B82F6");
        costsEstimatedLabel = (Label) costBox.getChildren().get(1);

        getChildren().addAll(symptomBox, analysisBox, reportBox, costBox);
    }

    private VBox createStatBox(String icon, String label, String value, String color) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(6, 12, 6, 12));
        box.setMinWidth(120);
        box.setMaxWidth(150);
        box.setPrefHeight(50);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
        box.getStyleClass().add("quick-stats-card");
        
        HBox iconValueBox = new HBox(8);
        iconValueBox.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 18));
        iconLabel.getStyleClass().add("quick-stats-icon");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        valueLabel.setTextFill(Color.web(color));
        valueLabel.getStyleClass().add("quick-stats-value");
        
        iconValueBox.getChildren().addAll(iconLabel, valueLabel);
        
        Label descLabel = new Label(label);
        descLabel.setFont(Font.font("System", 10));
        descLabel.setTextFill(Color.GRAY);
        descLabel.getStyleClass().add("quick-stats-label");
        
        box.getChildren().addAll(iconValueBox, descLabel);
        return box;
    }

    public void incrementSymptomsChecked() {
        symptomsChecked++;
        symptomsCheckedLabel.setText(String.valueOf(symptomsChecked));
    }

    public void incrementAnalysesDone() {
        analysesDone++;
        analysesDoneLabel.setText(String.valueOf(analysesDone));
    }

    public void incrementReportsAnalyzed() {
        reportsAnalyzed++;
        reportsAnalyzedLabel.setText(String.valueOf(reportsAnalyzed));
    }

    public void incrementCostsEstimated() {
        costsEstimated++;
        costsEstimatedLabel.setText(String.valueOf(costsEstimated));
    }

    public void resetStats() {
        symptomsChecked = 0;
        analysesDone = 0;
        reportsAnalyzed = 0;
        costsEstimated = 0;
        symptomsCheckedLabel.setText("0");
        analysesDoneLabel.setText("0");
        reportsAnalyzedLabel.setText("0");
        costsEstimatedLabel.setText("0");
    }
}


package com.smartheal.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AboutDialog {
    public static void show(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("About SMART Health Guide+");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF, #E0F2FE);");

        // App Icon/Title
        Label titleLabel = new Label("🏥 SMART Health Guide+");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.rgb(15, 118, 110));

        Label versionLabel = new Label("Version 1.0.0");
        versionLabel.setFont(Font.font("System", 14));
        versionLabel.setTextFill(Color.GRAY);

        Label subtitleLabel = new Label("AI-Powered Medical Information System");
        subtitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        subtitleLabel.setTextFill(Color.rgb(30, 64, 175));

        // Features
        VBox featuresBox = new VBox(10);
        featuresBox.setAlignment(Pos.CENTER_LEFT);
        featuresBox.setPadding(new Insets(20));
        featuresBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label featuresTitle = new Label("Features:");
        featuresTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        featuresTitle.setTextFill(Color.rgb(15, 118, 110));

        Label feature1 = new Label("• AI-Powered Symptom Checker");
        Label feature2 = new Label("• Intelligent Health Chatbot");
        Label feature3 = new Label("• Medical Test Recommendations");
        Label feature4 = new Label("• Treatment Cost Estimator");
        Label feature5 = new Label("• Blood Report Analyzer");

        featuresBox.getChildren().addAll(featuresTitle, feature1, feature2, feature3, feature4, feature5);

        // Technology
        Label techLabel = new Label("Built with JavaFX, Python Flask, and Machine Learning");
        techLabel.setFont(Font.font("System", 11));
        techLabel.setTextFill(Color.GRAY);
        techLabel.setWrapText(true);

        // Disclaimer
        Label disclaimerLabel = new Label("⚠️ This application is for educational purposes only and should not replace professional medical advice.");
        disclaimerLabel.setFont(Font.font("System", 10));
        disclaimerLabel.setTextFill(Color.rgb(185, 28, 28));
        disclaimerLabel.setWrapText(true);
        disclaimerLabel.setStyle("-fx-padding: 10; -fx-background-color: #FEF2F2; -fx-background-radius: 5;");

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #14B8A6, #22D3EE); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;"));

        root.getChildren().addAll(titleLabel, versionLabel, subtitleLabel, featuresBox, techLabel, disclaimerLabel, closeButton);

        Scene scene = new Scene(root, 500, 550);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}



package com.smartheal.views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DisclaimerBanner extends VBox {
    public DisclaimerBanner() {
        setStyle("-fx-background-color: linear-gradient(to right, #E0F2FE, #F0FDFA); -fx-border-color: #14B8A6; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.1), 4, 0, 0, 2);");
        setPadding(new Insets(12));
        setSpacing(5);

        Label titleLabel = new Label("ℹ️ Educational Purposes Only:");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.rgb(15, 118, 110));

        Label descriptionLabel = new Label(
            "This tool provides general health information and is not a substitute for professional medical advice, " +
            "diagnosis, or treatment. Always consult with a qualified healthcare provider for medical concerns."
        );
        descriptionLabel.setWrapText(true);
        descriptionLabel.setFont(Font.font("System", 12));
        descriptionLabel.setTextFill(Color.rgb(30, 64, 175));

        getChildren().addAll(titleLabel, descriptionLabel);
    }
}


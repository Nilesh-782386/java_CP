package com.smartheal.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class NotificationHelper {
    public static void showSuccessNotification(StackPane rootPane, String message) {
        Platform.runLater(() -> {
            VBox notification = new VBox(8);
            notification.setAlignment(Pos.CENTER);
            notification.setPadding(new Insets(15, 25, 15, 25));
            notification.setStyle("-fx-background-color: linear-gradient(to bottom, #22C55E, #16A34A); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(34, 197, 94, 0.4), 10, 0, 0, 4);");
            
            Label iconLabel = new Label("✓");
            iconLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            iconLabel.setTextFill(Color.WHITE);
            
            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            messageLabel.setTextFill(Color.WHITE);
            messageLabel.setWrapText(true);
            
            notification.getChildren().addAll(iconLabel, messageLabel);
            notification.setMaxWidth(350);
            
            StackPane.setAlignment(notification, Pos.TOP_CENTER);
            StackPane.setMargin(notification, new Insets(20, 0, 0, 0));
            rootPane.getChildren().add(notification);
            
            // Animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notification);
            slideIn.setFromY(-50);
            slideIn.setToY(0);
            
            ParallelTransition show = new ParallelTransition(fadeIn, slideIn);
            show.play();
            
            // Auto-hide after 3 seconds
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notification);
            slideOut.setFromY(0);
            slideOut.setToY(-50);
            
            ParallelTransition hide = new ParallelTransition(fadeOut, slideOut);
            hide.setDelay(Duration.seconds(3));
            hide.setOnFinished(e -> rootPane.getChildren().remove(notification));
            hide.play();
        });
    }

    public static void showInfoNotification(StackPane rootPane, String message) {
        Platform.runLater(() -> {
            VBox notification = new VBox(8);
            notification.setAlignment(Pos.CENTER);
            notification.setPadding(new Insets(15, 25, 15, 25));
            notification.setStyle("-fx-background-color: linear-gradient(to bottom, #3B82F6, #2563EB); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 10, 0, 0, 4);");
            
            Label iconLabel = new Label("ℹ");
            iconLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            iconLabel.setTextFill(Color.WHITE);
            
            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            messageLabel.setTextFill(Color.WHITE);
            messageLabel.setWrapText(true);
            
            notification.getChildren().addAll(iconLabel, messageLabel);
            notification.setMaxWidth(350);
            
            StackPane.setAlignment(notification, Pos.TOP_CENTER);
            StackPane.setMargin(notification, new Insets(20, 0, 0, 0));
            rootPane.getChildren().add(notification);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notification);
            slideIn.setFromY(-50);
            slideIn.setToY(0);
            
            ParallelTransition show = new ParallelTransition(fadeIn, slideIn);
            show.play();
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notification);
            slideOut.setFromY(0);
            slideOut.setToY(-50);
            
            ParallelTransition hide = new ParallelTransition(fadeOut, slideOut);
            hide.setDelay(Duration.seconds(3));
            hide.setOnFinished(e -> rootPane.getChildren().remove(notification));
            hide.play();
        });
    }
}


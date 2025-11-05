package com.smartheal.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class NotificationHelper {
    
    public enum NotificationType {
        SUCCESS, ERROR, INFO, WARNING
    }
    
    public static void showSuccessNotification(StackPane rootPane, String message) {
        showNotification(rootPane, message, NotificationType.SUCCESS);
    }
    
    public static void showErrorNotification(StackPane rootPane, String message) {
        showNotification(rootPane, message, NotificationType.ERROR);
    }
    
    public static void showInfoNotification(StackPane rootPane, String message) {
        showNotification(rootPane, message, NotificationType.INFO);
    }
    
    public static void showWarningNotification(StackPane rootPane, String message) {
        showNotification(rootPane, message, NotificationType.WARNING);
    }
    
    private static void showNotification(StackPane rootPane, String message, NotificationType type) {
        Platform.runLater(() -> {
            // Create modern notification card
            VBox notification = new VBox(0);
            notification.setMaxWidth(400);
            notification.setMinWidth(350);
            
            // Get type-specific colors and icons
            String bgColor, borderColor, iconColor, icon, title;
            switch (type) {
                case SUCCESS:
                    bgColor = "#FFFFFF";
                    borderColor = "#22C55E";
                    iconColor = "#22C55E";
                    icon = "✓";
                    title = "Success";
                    break;
                case ERROR:
                    bgColor = "#FFFFFF";
                    borderColor = "#EF4444";
                    iconColor = "#EF4444";
                    icon = "✕";
                    title = "Error";
                    break;
                case WARNING:
                    bgColor = "#FFFFFF";
                    borderColor = "#F59E0B";
                    iconColor = "#F59E0B";
                    icon = "⚠";
                    title = "Warning";
                    break;
                default: // INFO
                    bgColor = "#FFFFFF";
                    borderColor = "#3B82F6";
                    iconColor = "#3B82F6";
                    icon = "ℹ";
                    title = "Info";
                    break;
            }
            
            // Main container with shadow and border
            notification.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);"
            );
            
            // Header with icon, title, and close button
            HBox headerBox = new HBox(12);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(15, 15, 10, 15));
            headerBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12 12 0 0;");
            
            // Icon circle
            StackPane iconContainer = new StackPane();
            iconContainer.setMinSize(36, 36);
            iconContainer.setMaxSize(36, 36);
            iconContainer.setStyle(
                "-fx-background-color: " + iconColor + "15; " +
                "-fx-background-radius: 18;"
            );
            
            Label iconLabel = new Label(icon);
            iconLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
            iconLabel.setTextFill(Color.web(iconColor));
            iconContainer.getChildren().add(iconLabel);
            
            // Title and message container
            VBox contentBox = new VBox(4);
            contentBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(contentBox, Priority.ALWAYS);
            
            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleLabel.setTextFill(Color.web("#1F2937"));
            
            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("System", 13));
            messageLabel.setTextFill(Color.web("#4B5563"));
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(300);
            
            contentBox.getChildren().addAll(titleLabel, messageLabel);
            
            // Close button
            Button closeButton = new Button("✕");
            closeButton.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #9CA3AF; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 2 8; " +
                "-fx-background-radius: 4;"
            );
            closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                "-fx-background-color: #F3F4F6; " +
                "-fx-text-fill: #374151; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 2 8; " +
                "-fx-background-radius: 4;"
            ));
            closeButton.setOnMouseExited(e -> closeButton.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #9CA3AF; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 2 8; " +
                "-fx-background-radius: 4;"
            ));
            
            headerBox.getChildren().addAll(iconContainer, contentBox, closeButton);
            
            notification.getChildren().add(headerBox);
            
            // Position at top-right
            StackPane.setAlignment(notification, Pos.TOP_RIGHT);
            StackPane.setMargin(notification, new Insets(20, 20, 0, 0));
            
            // Add to root
            rootPane.getChildren().add(notification);
            
            // Close button action
            closeButton.setOnAction(e -> hideNotification(rootPane, notification));
            
            // Animation - slide from right
            notification.setTranslateX(500);
            notification.setOpacity(0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), notification);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), notification);
            slideIn.setFromX(500);
            slideIn.setToX(0);
            slideIn.setInterpolator(javafx.animation.Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0));
            
            ParallelTransition show = new ParallelTransition(fadeIn, slideIn);
            show.play();
            
            // Auto-hide after 4 seconds
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notification);
            slideOut.setFromX(0);
            slideOut.setToX(500);
            slideOut.setInterpolator(javafx.animation.Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0));
            
            ParallelTransition hide = new ParallelTransition(fadeOut, slideOut);
            hide.setDelay(Duration.seconds(4));
            hide.setOnFinished(e -> hideNotification(rootPane, notification));
            hide.play();
        });
    }
    
    private static void hideNotification(StackPane rootPane, VBox notification) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
        fadeOut.setFromValue(notification.getOpacity());
        fadeOut.setToValue(0);
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notification);
        slideOut.setFromX(notification.getTranslateX());
        slideOut.setToX(500);
        slideOut.setInterpolator(javafx.animation.Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0));
        
        ParallelTransition hide = new ParallelTransition(fadeOut, slideOut);
        hide.setOnFinished(e -> {
            if (rootPane.getChildren().contains(notification)) {
                rootPane.getChildren().remove(notification);
            }
        });
        hide.play();
    }
}


package com.smartheal.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusBar extends HBox {
    private Label statusLabel;
    private Label timeLabel;
    private Label connectionLabel;
    private Timeline clockTimeline;

    public StatusBar() {
        setStyle(
            "-fx-background-color: linear-gradient(to right, #0F766E, #14B8A6); " +
            "-fx-padding: 8 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, -2); " +
            "-fx-opacity: 1.0; " +
            "-fx-background-insets: 0;"
        );
        setSpacing(15);
        setAlignment(Pos.CENTER_LEFT);
        
        // Ensure status bar is always visible and stable
        setManaged(true);
        setVisible(true);
        setOpacity(1.0);
        setPickOnBounds(false);  // Don't interfere with mouse events on other elements
        setMouseTransparent(false);  // Allow status bar to receive events
        setFocusTraversable(false);

        // Connection status
        connectionLabel = new Label("● Offline");
        connectionLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold; -fx-font-size: 12px;");
        connectionLabel.setTooltip(new javafx.scene.control.Tooltip("Backend connection status"));
        connectionLabel.setManaged(true);
        connectionLabel.setVisible(true);

        // Status message
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        statusLabel.setManaged(true);
        statusLabel.setVisible(true);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        // Time label - always visible and stable
        timeLabel = new Label("🕐 00:00:00");
        timeLabel.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-opacity: 1.0; " +  // Ensure full opacity
            "-fx-min-width: 95px; " +  // Fixed width to prevent layout shifts
            "-fx-pref-width: 95px; " +
            "-fx-max-width: 95px; " +
            "-fx-alignment: center-right; " +
            "-fx-padding: 0 5 0 0; " +  // Add padding for stability
            "-fx-background-color: transparent;"
        );
        timeLabel.setManaged(true);
        timeLabel.setVisible(true);
        timeLabel.setOpacity(1.0);
        timeLabel.setPickOnBounds(false);  // Don't capture mouse events
        timeLabel.setMouseTransparent(true);  // Make mouse-transparent to prevent hover interference
        timeLabel.setFocusTraversable(false);
        
        // Initialize time
        updateTime();

        // Clock update timeline - update directly without double Platform.runLater
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();

        getChildren().addAll(connectionLabel, statusLabel, timeLabel);
    }

    private void updateTime() {
        // Always update - don't check visibility as it might be temporarily false
        if (timeLabel != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String timeText = "🕐 " + sdf.format(new Date());
                Platform.runLater(() -> {
                    if (timeLabel != null) {
                        timeLabel.setText(timeText);
                        // Force visibility after every update
                        timeLabel.setVisible(true);
                        timeLabel.setManaged(true);
                        timeLabel.setOpacity(1.0);
                    }
                });
            } catch (Exception e) {
                // Silently handle any update errors
                System.err.println("Error updating time: " + e.getMessage());
            }
        }
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionLabel.setText("● Online");
            connectionLabel.setStyle("-fx-text-fill: #86EFAC; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            connectionLabel.setText("● Offline");
            connectionLabel.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
    }

    public void stop() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }
}



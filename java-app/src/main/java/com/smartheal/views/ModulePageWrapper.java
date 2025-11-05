package com.smartheal.views;

import com.smartheal.components.StatusBar;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class ModulePageWrapper extends BorderPane {
    private String moduleTitle;
    private String moduleIcon;
    private javafx.scene.Node moduleContent;
    private Runnable onBackClick;
    private StatusBar statusBar;

    public ModulePageWrapper(String moduleTitle, String moduleIcon, javafx.scene.Node moduleContent, Runnable onBackClick) {
        this.moduleTitle = moduleTitle;
        this.moduleIcon = moduleIcon;
        this.moduleContent = moduleContent;
        this.onBackClick = onBackClick;
        
        createWrapper();
    }

    private void createWrapper() {
        // Set background
        setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF 0%, #E0F2FE 50%, #F0F9FF 100%);");

        // Create sticky header
        HBox headerBox = createHeader();
        setTop(headerBox);

        // Set module content
        setCenter(moduleContent);

        // Status bar at bottom - always visible and stable
        statusBar = new StatusBar();
        statusBar.setManaged(true);
        statusBar.setVisible(true);
        statusBar.setOpacity(1.0);
        statusBar.setPickOnBounds(false);
        
        // Wrap status bar in a container to ensure it's always stable
        VBox statusContainer = new VBox(statusBar);
        statusContainer.setManaged(true);
        statusContainer.setVisible(true);
        statusContainer.setOpacity(1.0);
        statusContainer.setStyle("-fx-background-color: transparent;");
        
        // Set as bottom of BorderPane - always stable
        setBottom(statusContainer);
    }

    private HBox createHeader() {
        HBox headerBox = new HBox(20);
        headerBox.setPadding(new Insets(15, 25, 15, 25));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle(
            "-fx-background-color: linear-gradient(to right, #0F766E, #14B8A6, #22D3EE); " +
            "-fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.3), 10, 0, 0, 4);"
        );

        // Back button
        Button backButton = new Button("← Back to Dashboard");
        backButton.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: rgba(255, 255, 255, 0.3); " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-cursor: hand;"
        );
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.3); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(255, 255, 255, 0.5); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );
        });
        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.2); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(255, 255, 255, 0.3); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: none;"
            );
        });
        backButton.setOnAction(e -> {
            if (onBackClick != null) {
                onBackClick.run();
            }
        });

        // Module title and icon
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(moduleIcon);
        iconLabel.setFont(Font.font("System", 28));
        iconLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");

        Label titleLabel = new Label(moduleTitle);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        titleBox.getChildren().addAll(iconLabel, titleLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backButton, titleBox, spacer);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        return headerBox;
    }

    public void setConnectionStatus(boolean connected) {
        if (statusBar != null) {
            statusBar.setConnectionStatus(connected);
        }
    }

    public void setStatus(String status) {
        if (statusBar != null) {
            statusBar.setStatus(status);
        }
    }
}


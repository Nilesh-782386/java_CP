package com.smartheal.views;

import com.smartheal.dao.UserDAO;
import com.smartheal.models.User;
import com.smartheal.utils.NotificationHelper;
import com.smartheal.utils.PasswordHasher;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class LoginView extends VBox {
    private final Stage primaryStage;
    private User currentUser;
    
    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupUI();
    }
    
    private void setupUI() {
        setAlignment(Pos.CENTER);
        setSpacing(25);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF, #E0F2FE);");
        
        // Header
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("🏥 SMART Health Guide+");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.rgb(15, 118, 110));
        
        Label subtitleLabel = new Label("AI-Powered Medical Advisor");
        subtitleLabel.setFont(Font.font("System", 16));
        subtitleLabel.setTextFill(Color.GRAY);
        
        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        
        // Login Form
        VBox loginForm = new VBox(20);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setMaxWidth(400);
        loginForm.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 35; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label loginTitle = new Label("Login");
        loginTitle.setFont(Font.font("System", FontWeight.BOLD, 24));
        loginTitle.setTextFill(Color.rgb(15, 118, 110));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(45);
        usernameField.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        
        CheckBox rememberMeCheck = new CheckBox("Remember me");
        rememberMeCheck.setStyle("-fx-font-size: 13;");
        
        Button loginButton = new Button("🔐 Login");
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(50);
        loginButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8; -fx-cursor: hand;");
        loginButton.setCursor(javafx.scene.Cursor.HAND);
        
        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setStyle("-fx-text-fill: #3B82F6; -fx-font-size: 13; -fx-underline: true;");
        
        loginForm.getChildren().addAll(loginTitle, usernameField, passwordField, rememberMeCheck, loginButton, registerLink);
        
        // Footer
        Label footerLabel = new Label("⚠️ This tool provides educational information only. Always consult a healthcare professional.");
        footerLabel.setWrapText(true);
        footerLabel.setAlignment(Pos.CENTER);
        footerLabel.setFont(Font.font("System", 11));
        footerLabel.setTextFill(Color.GRAY);
        footerLabel.setMaxWidth(400);
        
        getChildren().addAll(headerBox, loginForm, footerLabel);
        
        // Event Handlers
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                showError("Validation Error", "Please enter both username and password");
                return;
            }
            
            loginButton.setDisable(true);
            loginButton.setText("Logging in...");
            
            new Thread(() -> {
                try {
                    UserDAO userDAO = new UserDAO();
                    Optional<User> user = userDAO.loginUser(username, password);
                    
                    Platform.runLater(() -> {
                        if (user.isPresent()) {
                            this.currentUser = user.get();
                            openMainApplication();
                        } else {
                            showError("Login Failed", "Invalid username or password");
                            loginButton.setDisable(false);
                            loginButton.setText("🔐 Login");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showError("Login Error", "An error occurred during login: " + ex.getMessage());
                        loginButton.setDisable(false);
                        loginButton.setText("🔐 Login");
                    });
                }
            }).start();
        });
        
        registerLink.setOnAction(e -> openRegisterView());
        
        // Enter key support
        passwordField.setOnAction(e -> loginButton.fire());
    }
    
    private void openMainApplication() {
        // This will be handled by the main application class
        // For now, we'll signal that login is successful
        primaryStage.setUserData(currentUser);
        primaryStage.close();
    }
    
    private void openRegisterView() {
        Stage registerStage = new Stage();
        RegisterView registerView = new RegisterView(registerStage, primaryStage);
        Scene registerScene = new Scene(registerView, 500, 650);
        registerStage.setScene(registerScene);
        registerStage.setTitle("Register - SMART Health Guide+");
        registerStage.setResizable(false);
        registerStage.initOwner(primaryStage);
        registerStage.show();
    }
    
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
}


package com.smartheal.views;

import com.smartheal.dao.UserDAO;
import com.smartheal.models.User;
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

import java.time.LocalDate;

public class RegisterView extends VBox {
    private final Stage registerStage;
    private final Stage loginStage;
    
    public RegisterView(Stage registerStage, Stage loginStage) {
        this.registerStage = registerStage;
        this.loginStage = loginStage;
        setupUI();
    }
    
    private void setupUI() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF, #E0F2FE);");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20));
        
        // Header
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.rgb(15, 118, 110));
        
        // Registration Form
        VBox registerForm = new VBox(15);
        registerForm.setAlignment(Pos.CENTER);
        registerForm.setMaxWidth(450);
        registerForm.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name *");
        fullNameField.setPrefHeight(40);
        styleTextField(fullNameField);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username *");
        usernameField.setPrefHeight(40);
        styleTextField(usernameField);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email *");
        emailField.setPrefHeight(40);
        styleTextField(emailField);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 chars, must contain letter & number) *");
        passwordField.setPrefHeight(40);
        styleTextField(passwordField);
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password *");
        confirmPasswordField.setPrefHeight(40);
        styleTextField(confirmPasswordField);
        
        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPromptText("Date of Birth (Optional)");
        dateOfBirthPicker.setPrefHeight(40);
        dateOfBirthPicker.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Other");
        genderCombo.setPromptText("Gender (Optional)");
        genderCombo.setPrefHeight(40);
        genderCombo.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        
        Button registerButton = new Button("✅ Register");
        registerButton.setPrefWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(50);
        registerButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8; -fx-cursor: hand;");
        registerButton.setCursor(javafx.scene.Cursor.HAND);
        
        Hyperlink loginLink = new Hyperlink("Already have an account? Login here");
        loginLink.setStyle("-fx-text-fill: #3B82F6; -fx-font-size: 13; -fx-underline: true;");
        
        registerForm.getChildren().addAll(
            titleLabel, fullNameField, usernameField, emailField, 
            passwordField, confirmPasswordField, dateOfBirthPicker, 
            genderCombo, registerButton, loginLink
        );
        
        contentBox.getChildren().add(registerForm);
        scrollPane.setContent(contentBox);
        getChildren().add(scrollPane);
        
        // Event Handlers
        registerButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            LocalDate dateOfBirth = dateOfBirthPicker.getValue();
            String gender = genderCombo.getValue();
            
            // Validation
            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Validation Error", "Please fill in all required fields (marked with *)");
                return;
            }
            
            if (!PasswordHasher.isValidPassword(password)) {
                showError("Invalid Password", "Password must be at least 6 characters and contain both letters and numbers");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showError("Password Mismatch", "Passwords do not match");
                return;
            }
            
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showError("Invalid Email", "Please enter a valid email address");
                return;
            }
            
            registerButton.setDisable(true);
            registerButton.setText("Registering...");
            
            new Thread(() -> {
                try {
                    UserDAO userDAO = new UserDAO();
                    
                    // Check if username exists
                    if (userDAO.usernameExists(username)) {
                        Platform.runLater(() -> {
                            showError("Registration Failed", "Username already exists. Please choose a different username.");
                            registerButton.setDisable(false);
                            registerButton.setText("✅ Register");
                        });
                        return;
                    }
                    
                    // Check if email exists
                    if (userDAO.emailExists(email)) {
                        Platform.runLater(() -> {
                            showError("Registration Failed", "Email already exists. Please use a different email.");
                            registerButton.setDisable(false);
                            registerButton.setText("✅ Register");
                        });
                        return;
                    }
                    
                    // Hash password
                    String passwordHash = PasswordHasher.hashPassword(password);
                    
                    // Create user
                    User newUser = new User(username, email, passwordHash, fullName, dateOfBirth, gender);
                    
                    // Register user
                    boolean success = userDAO.registerUser(newUser);
                    
                    Platform.runLater(() -> {
                        if (success) {
                            showSuccess("Registration Successful", "Account created successfully! Please login.");
                            registerStage.close();
                            loginStage.show();
                        } else {
                            showError("Registration Failed", "Failed to create account. Please try again.");
                            registerButton.setDisable(false);
                            registerButton.setText("✅ Register");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        String errorMsg = ex.getMessage();
                        if (errorMsg != null && errorMsg.contains("already exists")) {
                            showError("Registration Failed", errorMsg);
                        } else {
                            showError("Registration Error", "An error occurred: " + ex.getMessage());
                        }
                        registerButton.setDisable(false);
                        registerButton.setText("✅ Register");
                    });
                }
            }).start();
        });
        
        loginLink.setOnAction(e -> {
            registerStage.close();
            loginStage.show();
        });
    }
    
    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
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
    
    private void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}


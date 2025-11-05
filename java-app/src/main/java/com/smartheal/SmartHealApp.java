package com.smartheal;

import com.smartheal.api.ApiClient;
import com.smartheal.components.AboutDialog;
import com.smartheal.components.QuickStatsPanel;
import com.smartheal.components.StatusBar;
import com.smartheal.database.DatabaseConnection;
import com.smartheal.database.DatabaseInitializer;
import com.smartheal.models.User;
import com.smartheal.views.*;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SmartHealApp extends Application {
    private ApiClient apiClient;
    private boolean backendAvailable = false;
    private StatusBar statusBar;
    private Stage primaryStage;
    private Timer connectionCheckTimer;
    private QuickStatsPanel statsPanel;
    private User currentUser;
    
    // Scene management
    private Scene currentScene;
    private DashboardView dashboardView;
    private StackPane notificationLayer;
    
    // Module views (lazy loaded)
    private SymptomCheckerView symptomCheckerView;
    private HealthChatbotView chatbotView;
    private TestLookupView testLookupView;
    private CostEstimatorView costEstimatorView;
    private ReportAnalyzerView reportAnalyzerView;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize database
        try {
            if (DatabaseConnection.testConnection()) {
                DatabaseInitializer.initializeDatabase();
            } else {
                showDatabaseWarning();
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            showDatabaseWarning();
        }
        
        // Show login screen first
        showLoginScreen();
    }
    
    private void showDatabaseWarning() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Database Warning");
            alert.setHeaderText("Database Connection Failed");
            alert.setContentText(
                "Could not connect to MySQL database.\n" +
                "Some features (login, history) will be unavailable.\n" +
                "Please ensure MySQL is running and credentials are correct.\n\n" +
                "Application will continue without database features."
            );
            alert.show();
        });
    }
    
    private void showUserProfile() {
        if (currentUser == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Profile");
        alert.setHeaderText("Profile Information");
        alert.setContentText(
            "Username: " + currentUser.getUsername() + "\n" +
            "Email: " + currentUser.getEmail() + "\n" +
            "Full Name: " + currentUser.getFullName() + "\n" +
            "Gender: " + (currentUser.getGender() != null ? currentUser.getGender() : "Not specified") + "\n" +
            "Date of Birth: " + (currentUser.getDateOfBirth() != null ? currentUser.getDateOfBirth().toString() : "Not specified")
        );
        alert.showAndWait();
    }
    
    private void showLoginScreen() {
        Stage loginStage = new Stage();
        LoginView loginView = new LoginView(loginStage);
        Scene loginScene = new Scene(loginView, 550, 600);
        loginStage.setScene(loginScene);
        loginStage.setTitle("Login - SMART Health Guide+");
        loginStage.setResizable(false);
        
        loginStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        
        loginStage.showAndWait();
        
        // Get user from login stage
        Object userData = loginStage.getUserData();
        if (userData instanceof User) {
            this.currentUser = (User) userData;
            // Start main application
            initializeMainApplication();
        } else {
            Platform.exit();
            System.exit(0);
        }
    }
    
    private void initializeMainApplication() {
        if (currentUser == null) {
            Platform.exit();
            return;
        }
        
        primaryStage.setTitle("SMART Health Guide+ - AI Medical Advisor - Welcome, " + currentUser.getFullName());
        
        // Continue with existing initialization
        try {
            initializeApplication();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    private void initializeApplication() {
        // Initialize API client with error handling
        try {
            apiClient = new ApiClient();
            backendAvailable = apiClient.isBackendAvailable();
            
            if (!backendAvailable) {
                Platform.runLater(this::showBackendWarning);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize API client: " + e.getMessage());
            e.printStackTrace();
            backendAvailable = false;
            apiClient = new ApiClient();
            Platform.runLater(this::showBackendWarning);
        }

        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);

        // Create notification layer (always on top)
        notificationLayer = new StackPane();
        notificationLayer.setStyle("-fx-background-color: transparent;");
        notificationLayer.setPickOnBounds(false); // Don't block mouse events
        notificationLayer.setMouseTransparent(false); // Allow mouse events to pass through to children

        // Create dashboard
        createDashboard();

        // Create scene and apply styles
        currentScene = new Scene(notificationLayer);
        applyStyles(currentScene);
        
        // Add keyboard shortcuts
        setupKeyboardShortcuts(currentScene);

        primaryStage.setScene(currentScene);
        primaryStage.show();

        // Start connection monitoring
        startConnectionMonitoring();

        // Set close handler
        primaryStage.setOnCloseRequest(e -> cleanup());
    }

    private void createDashboard() {
        // Create dashboard view
        dashboardView = new DashboardView();
        
        // Set up navigation callbacks
        dashboardView.setOnSymptomCheckerClick(() -> {
            System.out.println("Symptom Checker callback triggered!");
            Platform.runLater(() -> openSymptomChecker());
        });
        dashboardView.setOnHealthChatbotClick(() -> {
            System.out.println("Health Chatbot callback triggered!");
            Platform.runLater(() -> openHealthChatbot());
        });
        dashboardView.setOnTestLookupClick(() -> {
            System.out.println("Test Lookup callback triggered!");
            Platform.runLater(() -> openTestLookup());
        });
        dashboardView.setOnCostEstimatorClick(() -> {
            System.out.println("Cost Estimator callback triggered!");
            Platform.runLater(() -> openCostEstimator());
        });
        dashboardView.setOnReportAnalyzerClick(() -> {
            System.out.println("Report Analyzer callback triggered!");
            Platform.runLater(() -> openReportAnalyzer());
        });
        
        // Add menu bar to dashboard
        BorderPane dashboardContainer = new BorderPane();
        dashboardContainer.setCenter(dashboardView);
        
        MenuBar menuBar = createMenuBar();
        dashboardContainer.setTop(menuBar);
        
        // Wrap in notification layer
        notificationLayer.getChildren().clear();
        notificationLayer.getChildren().add(dashboardContainer);
    }

    private void openSymptomChecker() {
        try {
            if (symptomCheckerView == null) {
                System.out.println("Creating new SymptomCheckerView...");
                symptomCheckerView = new SymptomCheckerView(apiClient);
                if (currentUser != null) {
                    symptomCheckerView.setCurrentUserId(currentUser.getId());
                }
                System.out.println("SymptomCheckerView created successfully");
            }
            
            System.out.println("Wrapping SymptomCheckerView in ModulePageWrapper...");
            ModulePageWrapper wrapper = new ModulePageWrapper(
                "Symptom Checker",
                "🩺",
                symptomCheckerView,
                this::backToDashboard
            );
            
            System.out.println("Switching to Symptom Checker module...");
            switchToModule(wrapper);
            System.out.println("Symptom Checker opened successfully");
        } catch (Exception e) {
            System.err.println("Error opening Symptom Checker: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to open Symptom Checker");
                alert.setContentText("An error occurred while opening the Symptom Checker:\n" + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void openHealthChatbot() {
        if (chatbotView == null) {
            chatbotView = new HealthChatbotView(apiClient);
            if (currentUser != null) {
                chatbotView.setCurrentUserId(currentUser.getId());
            }
        }
        
        ModulePageWrapper wrapper = new ModulePageWrapper(
            "Health Chatbot",
            "💬",
            chatbotView,
            this::backToDashboard
        );
        
        switchToModule(wrapper);
    }

    private void openTestLookup() {
        if (testLookupView == null) {
            testLookupView = new TestLookupView(apiClient);
        }
        
        ModulePageWrapper wrapper = new ModulePageWrapper(
            "Test Lookup",
            "🔍",
            testLookupView,
            this::backToDashboard
        );
        
        switchToModule(wrapper);
    }

    private void openCostEstimator() {
        if (costEstimatorView == null) {
            costEstimatorView = new CostEstimatorView(apiClient);
            if (currentUser != null) {
                costEstimatorView.setCurrentUserId(currentUser.getId());
            }
        }
        
        ModulePageWrapper wrapper = new ModulePageWrapper(
            "Cost Estimator",
            "💰",
            costEstimatorView,
            this::backToDashboard
        );
        
        switchToModule(wrapper);
    }

    private void openReportAnalyzer() {
        if (reportAnalyzerView == null) {
            reportAnalyzerView = new ReportAnalyzerView(apiClient);
            if (currentUser != null) {
                reportAnalyzerView.setCurrentUserId(currentUser.getId());
            }
        }
        
        ModulePageWrapper wrapper = new ModulePageWrapper(
            "Report Analyzer",
            "📊",
            reportAnalyzerView,
            this::backToDashboard
        );
        
        switchToModule(wrapper);
    }

    private void switchToModule(ModulePageWrapper wrapper) {
        // Update status bar in wrapper
        wrapper.setConnectionStatus(backendAvailable);
        
        // Add menu bar
        BorderPane container = new BorderPane();
        container.setCenter(wrapper);
        
        MenuBar menuBar = createMenuBar();
        container.setTop(menuBar);
        
        // Fade transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), notificationLayer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            notificationLayer.getChildren().clear();
            notificationLayer.getChildren().add(container);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notificationLayer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void backToDashboard() {
        createDashboard();
        
        // Fade transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), notificationLayer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notificationLayer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: linear-gradient(to right, #0F766E, #14B8A6);");

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem aboutItem = new MenuItem("About SMART Health Guide+");
        aboutItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        aboutItem.setOnAction(e -> AboutDialog.show(primaryStage));
        
        MenuItem profileItem = new MenuItem("My Profile");
        profileItem.setOnAction(e -> showUserProfile());
        
        MenuItem resetStatsItem = new MenuItem("Reset Statistics");
        resetStatsItem.setOnAction(e -> {
            if (statsPanel != null) {
                statsPanel.resetStats();
                com.smartheal.utils.UsageTracker.reset();
            }
        });
        
        MenuItem dashboardItem = new MenuItem("Dashboard");
        dashboardItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        dashboardItem.setOnAction(e -> backToDashboard());
        
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            currentUser = null;
            primaryStage.close();
            showLoginScreen();
        });
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        exitItem.setOnAction(e -> primaryStage.close());
        
        fileMenu.getItems().addAll(aboutItem, profileItem, resetStatsItem, new SeparatorMenuItem(), dashboardItem, new SeparatorMenuItem(), logoutItem, new SeparatorMenuItem(), exitItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem helpItem = new MenuItem("Help");
        helpItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        helpItem.setOnAction(e -> showHelpDialog());
        MenuItem aboutHelpItem = new MenuItem("About");
        aboutHelpItem.setOnAction(e -> AboutDialog.show(primaryStage));
        helpMenu.getItems().addAll(helpItem, aboutHelpItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private void setupKeyboardShortcuts(Scene scene) {
        // F1 - About
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), () -> {
            Platform.runLater(() -> AboutDialog.show(primaryStage));
        });

        // Ctrl+D - Dashboard
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> {
            Platform.runLater(this::backToDashboard);
        });

        // Ctrl+Q - Exit
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN), () -> {
            Platform.runLater(() -> primaryStage.close());
        });
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help - SMART Health Guide+");
        alert.setHeaderText("Keyboard Shortcuts");
        alert.setContentText(
            "F1 - Show About Dialog\n" +
            "Ctrl+D - Return to Dashboard\n" +
            "Ctrl+Q - Exit Application\n\n" +
            "Features:\n" +
            "1. Symptom Checker - Select symptoms to get AI-powered condition analysis\n" +
            "2. Health Chatbot - Ask medical questions and get AI responses\n" +
            "3. Test Lookup - Find recommended medical tests for diseases\n" +
            "4. Cost Estimator - Estimate treatment costs for different hospital types\n" +
            "5. Report Analyzer - Analyze blood test reports with AI insights\n\n" +
            "Note: Ensure Python backend is running on port 5000 for full functionality."
        );
        alert.showAndWait();
    }

    private void startConnectionMonitoring() {
        connectionCheckTimer = new Timer(true);
        connectionCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (apiClient != null) {
                    boolean connected = apiClient.isBackendAvailable();
                    Platform.runLater(() -> {
                        if (statusBar != null) {
                            statusBar.setConnectionStatus(connected);
                        }
                        
                        // Update status in current module wrapper if needed
                        javafx.scene.Node currentContent = notificationLayer.getChildren().isEmpty() ? null : notificationLayer.getChildren().get(0);
                        if (currentContent instanceof BorderPane) {
                            BorderPane container = (BorderPane) currentContent;
                            javafx.scene.Node center = container.getCenter();
                            if (center instanceof ModulePageWrapper) {
                                ((ModulePageWrapper) center).setConnectionStatus(connected);
                            }
                        }
                        
                        if (connected && !backendAvailable) {
                            if (statusBar != null) {
                                statusBar.setStatus("Backend connected successfully!");
                            }
                            backendAvailable = true;
                        } else if (!connected && backendAvailable) {
                            if (statusBar != null) {
                                statusBar.setStatus("Backend connection lost");
                            }
                            backendAvailable = false;
                        }
                    });
                }
            }
        }, 5000, 10000); // Check every 10 seconds after initial 5 second delay
    }

    private void applyStyles(Scene scene) {
        // Try multiple possible CSS paths
        String[] possibleCssPaths = {
            "/styles.css",  // Standard Maven/Gradle resources path
            "styles.css",   // Alternative path
            "/com/smartheal/styles/styles.css"  // Package-based path
        };
        
        boolean cssLoaded = false;
        
        for (String cssPath : possibleCssPaths) {
            try {
                URL cssUrl = getClass().getResource(cssPath);
                if (cssUrl != null) {
                    String externalForm = cssUrl.toExternalForm();
                    scene.getStylesheets().add(externalForm);
                    System.out.println("✓ Loaded CSS from: " + cssPath);
                    cssLoaded = true;
                    break;
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }
        
        if (!cssLoaded) {
            System.err.println("⚠ Warning: Could not load CSS file from any of the attempted paths:");
            for (String path : possibleCssPaths) {
                System.err.println("  - " + path);
            }
            System.err.println("Application will continue without custom styles.");
        }
    }

    private void showBackendWarning() {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Backend Connection Warning");
            alert.setHeaderText("Cannot Connect to Backend Server");
            alert.setContentText(
                "The application cannot connect to the Python backend server at http://localhost:5000.\n\n" +
                "Please ensure:\n" +
                "1. The Python backend is running (python app.py in backend_python directory)\n" +
                "2. The Python backend is accessible on port 5000\n" +
                "3. No firewall is blocking the connection\n\n" +
                "You can still use the application, but API features will not work.\n\n" +
                "The application will continue in offline mode."
            );
            
            // Show asynchronously and non-blocking
            alert.show();
        } catch (Exception e) {
            System.err.println("Failed to show backend warning: " + e.getMessage());
        }
    }

    private void cleanup() {
        try {
            if (connectionCheckTimer != null) {
                connectionCheckTimer.cancel();
            }
            if (statusBar != null) {
                statusBar.stop();
            }
            if (apiClient != null) {
                apiClient.close();
            }
            // Close database connection
            DatabaseConnection.closeConnection();
        } catch (Exception ex) {
            System.err.println("Error during cleanup: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Starting SmartHeal Application...");
            launch(args);
        } catch (Exception e) {
            System.err.println("Application failed to start: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

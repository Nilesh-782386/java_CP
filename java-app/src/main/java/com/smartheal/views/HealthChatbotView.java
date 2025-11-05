package com.smartheal.views;

import com.smartheal.api.ApiClient;
import com.smartheal.dao.HistoryDAO;
import com.smartheal.models.ChatMessage;
import com.smartheal.models.ChatResponse;
import com.smartheal.utils.NotificationHelper;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HealthChatbotView extends BorderPane {
    private final ApiClient apiClient;
    private final List<ChatMessage> messages;
    private final HistoryDAO historyDAO;
    private Integer currentUserId = null;
    
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
    
    private ListView<HBox> messagesList;
    private TextArea inputArea;
    private Button sendButton;
    private ProgressIndicator loadingIndicator;

    private static final String[] SUGGESTED_QUESTIONS = {
        "What medicine should I take for fever?",
        "How to treat cough and which tablets to use?",
        "What are the symptoms of diabetes and treatment?",
        "How to manage high blood pressure?",
        "What medicine helps with headache?",
        "How to treat cold and flu?",
        "What causes joint pain and how to treat it?",
        "How to manage high cholesterol?",
        "What medicine for sore throat?",
        "How to prevent common diseases?"
    };

    public HealthChatbotView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.messages = new ArrayList<>();
        this.historyDAO = new HistoryDAO();

        VBox headerBox = createHeader();
        setTop(headerBox);

        VBox mainContent = createMainContent();
        setCenter(mainContent);
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.getStyleClass().add("header-section");
        headerBox.setPadding(new Insets(15, 20, 12, 20));

        Label titleLabel = new Label("💬 Health Chatbot");
        titleLabel.getStyleClass().add("header-title");

        Label descriptionLabel = new Label(
            "Ask questions about health and wellness. Get instant educational answers from our knowledge base."
        );
        descriptionLabel.getStyleClass().add("header-description");

        headerBox.getChildren().addAll(titleLabel, descriptionLabel, new DisclaimerBanner());

        return headerBox;
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(12);
        mainContent.setPadding(new Insets(15));
        mainContent.setStyle("-fx-background-color: #F0F9FF;");

        VBox chatBox = new VBox(12);
        chatBox.getStyleClass().add("panel");
        chatBox.setMaxWidth(Double.MAX_VALUE);
        chatBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(chatBox, Priority.ALWAYS);
        
        // Make chat box responsive
        chatBox.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.95));

        messagesList = new ListView<>();
        messagesList.setPrefHeight(400);
        messagesList.setCellFactory(param -> new MessageCell());
        messagesList.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(messagesList, Priority.ALWAYS);

        // Show initial welcome message
        showWelcomeMessage();

        HBox inputBox = new HBox(12);
        inputBox.setAlignment(Pos.CENTER_LEFT);
        inputBox.setPadding(new Insets(5, 0, 0, 0));

        inputArea = new TextArea();
        inputArea.setPromptText("Type your health question here...");
        inputArea.setPrefRowCount(3);
        inputArea.setWrapText(true);
        inputArea.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(inputArea, Priority.ALWAYS);

        VBox buttonBox = new VBox(5);
        buttonBox.setAlignment(Pos.CENTER);
        sendButton = new Button("💬 Send");
        sendButton.getStyleClass().addAll("button", "button-primary");
        sendButton.setStyle("-fx-background-color: linear-gradient(to bottom, #0F766E, #14B8A6); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        sendButton.setPrefHeight(50);
        sendButton.setPrefWidth(100);
        sendButton.setCursor(javafx.scene.Cursor.HAND);
        sendButton.setTooltip(new javafx.scene.control.Tooltip("Send message (Enter key)"));
        sendButton.setOnAction(e -> sendMessage());

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(20, 20);
        buttonBox.getChildren().addAll(sendButton, loadingIndicator);

        inputArea.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && !e.isShiftDown()) {
                sendMessage();
                e.consume();
            }
        });

        inputBox.getChildren().addAll(inputArea, buttonBox);

        Label hintLabel = new Label("💡 Press Enter to send, Shift+Enter for new line");
        hintLabel.setFont(Font.font("System", 10));
        hintLabel.setTextFill(Color.GRAY);

        chatBox.getChildren().addAll(messagesList, inputBox, hintLabel);

        mainContent.getChildren().add(chatBox);

        return mainContent;
    }

    private void showWelcomeMessage() {
        VBox welcomeBox = new VBox(15);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(40));
        welcomeBox.setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF, #F0FDFA); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.1), 8, 0, 0, 3);");

        Label welcomeLabel = new Label("👋 Welcome to Health Assistant");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        welcomeLabel.setTextFill(Color.rgb(15, 118, 110));

        Label descLabel = new Label("Ask me about health conditions, symptoms, treatments, and preventive medicines. Get detailed information about medications and dosages.");
        descLabel.setFont(Font.font("System", 12));
        descLabel.setTextFill(Color.GRAY);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(600);

        VBox suggestedBox = new VBox(10);
        Label suggestedLabel = new Label("Try asking:");
        suggestedLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        suggestedBox.getChildren().add(suggestedLabel);

        FlowPane questionsFlow = new FlowPane(10, 10);
        questionsFlow.setHgap(10);
        questionsFlow.setVgap(10);

        for (String question : SUGGESTED_QUESTIONS) {
            Button questionButton = new Button(question);
            questionButton.setStyle("-fx-background-color: linear-gradient(to bottom, white, #F0FDFA); -fx-border-color: #14B8A6; -fx-border-width: 2; -fx-padding: 10 18; -fx-background-radius: 18; -fx-text-fill: #0F766E; -fx-font-weight: 600; -fx-font-size: 13px; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.1), 3, 0, 0, 1); -fx-cursor: hand;");
            questionButton.setOnMouseEntered(e -> questionButton.setStyle("-fx-background-color: linear-gradient(to bottom, #E0F2FE, #F0FDFA); -fx-border-color: #0F766E; -fx-border-width: 2.5; -fx-padding: 10 18; -fx-background-radius: 18; -fx-text-fill: #0F766E; -fx-font-weight: 700; -fx-font-size: 13px; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.2), 5, 0, 0, 2); -fx-cursor: hand;"));
            questionButton.setOnMouseExited(e -> questionButton.setStyle("-fx-background-color: linear-gradient(to bottom, white, #F0FDFA); -fx-border-color: #14B8A6; -fx-border-width: 2; -fx-padding: 10 18; -fx-background-radius: 18; -fx-text-fill: #0F766E; -fx-font-weight: 600; -fx-font-size: 13px; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.1), 3, 0, 0, 1); -fx-cursor: hand;"));
            questionButton.setCursor(javafx.scene.Cursor.HAND);
            questionButton.setOnAction(e -> {
                inputArea.setText(question);
                sendMessage();
            });
            questionsFlow.getChildren().add(questionButton);
        }

        suggestedBox.getChildren().add(questionsFlow);
        welcomeBox.getChildren().addAll(welcomeLabel, descLabel, suggestedBox);

        HBox welcomeContainer = new HBox(welcomeBox);
        welcomeContainer.setAlignment(Pos.CENTER);
        messagesList.getItems().add(welcomeContainer);
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        
        if (text.length() > 500) {
            showError("Message Too Long", "Please keep messages under 500 characters.");
            return;
        }

        // Remove welcome message if it's the first user message
        if (messages.isEmpty() && messagesList.getItems().size() > 0) {
            messagesList.getItems().clear();
        }

        ChatMessage userMessage = new ChatMessage(
            String.valueOf(System.currentTimeMillis()),
            "user",
            text,
            System.currentTimeMillis()
        );
        messages.add(userMessage);
        updateMessagesDisplay();

        inputArea.clear();
        sendButton.setDisable(true);
        loadingIndicator.setVisible(true);
        
        NotificationHelper.showInfoNotification(
            (StackPane) getScene().getRoot(),
            "Processing your question... Please wait."
        );

        new Thread(() -> {
            try {
                ChatResponse response = apiClient.chat(text);
                if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
                    throw new IOException("Empty response from server");
                }
                
                ChatMessage assistantMessage = new ChatMessage(
                    String.valueOf(System.currentTimeMillis()),
                    "assistant",
                    response.getResponse(),
                    System.currentTimeMillis()
                );
                messages.add(assistantMessage);

                Platform.runLater(() -> {
                    updateMessagesDisplay();
                    sendButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                    messagesList.scrollTo(messagesList.getItems().size() - 1);
                    
                    // Save chat history if user is logged in
                    if (currentUserId != null) {
                        try {
                            historyDAO.saveChatHistory(currentUserId, text, response.getResponse());
                        } catch (Exception e) {
                            System.err.println("Failed to save chat history: " + e.getMessage());
                        }
                    }
                    
                    NotificationHelper.showSuccessNotification(
                        (StackPane) getScene().getRoot(),
                        "Response received successfully!"
                    );
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    String errorMsg = "Failed to get response from chatbot:\n" + e.getMessage();
                    
                    if (e.getMessage().contains("Connection refused") || 
                        e.getMessage().contains("timeout") ||
                        e.getMessage().contains("Cannot connect")) {
                        errorMsg += "\n\nBackend Connection Issue:\n" +
                                   "1. Ensure Python backend is running on port 5000\n" +
                                   "2. Check: http://localhost:5000/api/chat in browser\n" +
                                   "3. Verify Python backend server started successfully (python app.py)";
                    }
                    
                    showError("Chat Error", errorMsg);
                    sendButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Unexpected Error", 
                        "An unexpected error occurred:\n" + e.getMessage() + 
                        "\n\nPlease try again or restart the application.");
                    sendButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void updateMessagesDisplay() {
        messagesList.getItems().clear();
        for (ChatMessage message : messages) {
            HBox messageBox = createMessageBox(message);
            messagesList.getItems().add(messageBox);
        }
    }

    private HBox createMessageBox(ChatMessage message) {
        HBox messageBox = new HBox(12);
        messageBox.setAlignment(message.getRole().equals("user") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(8, 10, 8, 10));
        messageBox.setMaxWidth(Double.MAX_VALUE);

        VBox messageContent = new VBox(6);
        messageContent.setMaxWidth(600);

        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font("System", 13));
        contentLabel.setLineSpacing(3);
        contentLabel.setPadding(new Insets(12, 18, 12, 18));
        contentLabel.setMaxWidth(580);

        if (message.getRole().equals("user")) {
            contentLabel.getStyleClass().add("message-bubble-user");
            contentLabel.setStyle("-fx-background-color: linear-gradient(to bottom right, #0F766E, #14B8A6); -fx-text-fill: white; -fx-background-radius: 20 20 4 20; -fx-effect: dropshadow(gaussian, rgba(15, 118, 110, 0.3), 5, 0, 0, 2); -fx-font-weight: 500;");
            messageContent.setAlignment(Pos.CENTER_RIGHT);
        } else {
            contentLabel.getStyleClass().add("message-bubble-bot");
            contentLabel.setStyle("-fx-background-color: linear-gradient(to bottom right, #F3F4F6, #E5E7EB); -fx-text-fill: #1F2937; -fx-background-radius: 20 20 20 4; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1); -fx-font-weight: 500;");
            messageContent.setAlignment(Pos.CENTER_LEFT);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Label timeLabel = new Label(sdf.format(new Date(message.getTimestamp())));
        timeLabel.setFont(Font.font("System", 10));
        timeLabel.setTextFill(Color.GRAY);
        timeLabel.setPadding(new Insets(0, 5, 0, 5));

        messageContent.getChildren().addAll(contentLabel, timeLabel);
        messageBox.getChildren().add(messageContent);

        return messageBox;
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    private static class MessageCell extends ListCell<HBox> {
        @Override
        protected void updateItem(HBox item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                setGraphic(item);
            }
        }
    }
}


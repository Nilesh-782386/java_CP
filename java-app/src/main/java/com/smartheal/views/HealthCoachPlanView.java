package com.smartheal.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartheal.utils.JSONExporter;
import com.smartheal.utils.NotificationHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HealthCoachPlanView extends VBox {

    private final JSONObject healthPlan;

    public HealthCoachPlanView(JSONObject healthPlan) {
        this.healthPlan = healthPlan;
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f8f9fa;");
        buildView();
    }

    private void buildView() {
        Label header = new Label("Your Personalized 30-Day Health Plan");
        header.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox summaryCard = createSummaryCard();
        VBox scheduleCard = createScheduleCard();
        VBox nutritionCard = createNutritionCard();
        VBox exerciseCard = createExerciseCard();
        VBox monitoringCard = createMonitoringCard();
        VBox milestonesCard = createMilestonesCard();
        VBox targetsCard = createTargetsCard();
        HBox actions = createActionButtons();

        getChildren().addAll(
            header,
            summaryCard,
            scheduleCard,
            nutritionCard,
            exerciseCard,
            monitoringCard,
            milestonesCard,
            targetsCard,
            actions
        );
    }

    private VBox createSummaryCard() {
        VBox card = createCard("Plan Summary");

        JSONObject summary = healthPlan.optJSONObject("summary");
        String summaryText = summary != null ? summary.optString("text",
            "Follow this structured plan to improve your health over the next 30 days.") :
            "Follow this structured plan to improve your health over the next 30 days.";

        Label summaryLabel = new Label(summaryText);
        summaryLabel.setWrapText(true);
        summaryLabel.setStyle("-fx-font-size: 14; -fx-line-spacing: 1.5;");

        String generated = healthPlan.optString("generated_at", null);
        LocalDate date = generated != null ? LocalDate.now() : LocalDate.now();
        Label dateLabel = new Label("Generated on: " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666; -fx-font-style: italic;");

        card.getChildren().addAll(summaryLabel, dateLabel);
        return card;
    }

    private VBox createScheduleCard() {
        VBox card = createCard("Weekly Schedule");
        JSONArray schedule = healthPlan.optJSONArray("weekly_schedule");
        if (schedule == null) {
            Label empty = new Label("Schedule data unavailable.");
            card.getChildren().add(empty);
            return card;
        }

        for (int i = 0; i < schedule.length(); i++) {
            JSONObject day = schedule.getJSONObject(i);
            card.getChildren().add(createDaySchedule(day));
        }
        return card;
    }

    private HBox createDaySchedule(JSONObject day) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label dayLabel = new Label(day.optString("day", "Day"));
        dayLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 90;");

        Label focusLabel = new Label(day.optString("focus", ""));
        focusLabel.setStyle("-fx-text-fill: #2c3e50; -fx-min-width: 150;");

        FlowPane activitiesPane = new FlowPane();
        activitiesPane.setHgap(5);
        activitiesPane.setVgap(5);
        activitiesPane.setPrefWrapLength(600);

        JSONArray activities = day.optJSONArray("activities");
        if (activities != null) {
            for (int j = 0; j < activities.length(); j++) {
                Label tag = createTag(activities.getString(j));
                activitiesPane.getChildren().add(tag);
            }
        }

        HBox.setHgrow(activitiesPane, Priority.ALWAYS);
        row.getChildren().addAll(dayLabel, focusLabel, activitiesPane);
        return row;
    }

    private VBox createNutritionCard() {
        VBox card = createCard("Nutrition Plan");
        JSONObject nutrition = healthPlan.optJSONObject("nutrition_plan");
        if (nutrition == null) {
            card.getChildren().add(new Label("Nutrition recommendations unavailable."));
            return card;
        }

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 10, 0));

        int row = 0;
        row = addNutritionRow(grid, row, "Breakfast", nutrition.optString("breakfast", "N/A"));
        row = addNutritionRow(grid, row, "Lunch", nutrition.optString("lunch", "N/A"));
        row = addNutritionRow(grid, row, "Dinner", nutrition.optString("dinner", "N/A"));
        row = addNutritionRow(grid, row, "Snacks", nutrition.optString("snacks", "N/A"));
        row = addNutritionRow(grid, row, "Hydration", nutrition.optString("hydration", "N/A"));
        row = addNutritionRow(grid, row, "Avoid", nutrition.optString("avoid", "N/A"));

        if (nutrition.has("carbohydrates")) {
            row = addNutritionRow(grid, row, "Carbohydrates", nutrition.optString("carbohydrates"));
        }
        if (nutrition.has("fats")) {
            row = addNutritionRow(grid, row, "Fats", nutrition.optString("fats"));
        }
        if (nutrition.has("sodium")) {
            row = addNutritionRow(grid, row, "Sodium", nutrition.optString("sodium"));
        }
        if (nutrition.has("special_notes")) {
            row = addNutritionRow(grid, row, "Special Notes", nutrition.optString("special_notes"));
        }

        card.getChildren().add(grid);
        return card;
    }

    private int addNutritionRow(GridPane grid, int row, String label, String value) {
        Label keyLabel = new Label(label + ":");
        keyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);

        grid.add(keyLabel, 0, row);
        grid.add(valueLabel, 1, row);
        return row + 1;
    }

    private VBox createExerciseCard() {
        VBox card = createCard("Exercise Routine");
        JSONObject exercise = healthPlan.optJSONObject("exercise_routine");
        if (exercise == null) {
            card.getChildren().add(new Label("Exercise recommendations unavailable."));
            return card;
        }

        VBox content = new VBox(8);
        addExerciseDetail(content, "Frequency", exercise.optString("frequency", "N/A"));
        addExerciseDetail(content, "Duration", exercise.optString("duration", "N/A"));
        addExerciseDetail(content, "Warmup", exercise.optString("warmup", "N/A"));
        addExerciseDetail(content, "Cooldown", exercise.optString("cooldown", "N/A"));

        if (exercise.has("cardio")) {
            addExerciseDetail(content, "Cardio", exercise.optString("cardio"));
        }
        if (exercise.has("strength")) {
            addExerciseDetail(content, "Strength", exercise.optString("strength"));
        }
        if (exercise.has("special_notes")) {
            addExerciseDetail(content, "Special Notes", exercise.optString("special_notes"));
        }

        card.getChildren().add(content);
        return card;
    }

    private void addExerciseDetail(VBox container, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);

        Label keyLabel = new Label(label + ":");
        keyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 110;");

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);

        row.getChildren().addAll(keyLabel, valueLabel);
        container.getChildren().add(row);
    }

    private VBox createMonitoringCard() {
        VBox card = createCard("Health Monitoring");
        JSONObject monitoring = healthPlan.optJSONObject("monitoring_plan");
        if (monitoring == null) {
            card.getChildren().add(new Label("Monitoring recommendations unavailable."));
            return card;
        }

        VBox list = new VBox(6);
        for (String key : monitoring.keySet()) {
            Label item = new Label("• " + key + ": " + monitoring.optString(key));
            item.setWrapText(true);
            list.getChildren().add(item);
        }

        card.getChildren().add(list);
        return card;
    }

    private VBox createMilestonesCard() {
        VBox card = createCard("30-Day Milestones");
        JSONArray milestones = healthPlan.optJSONArray("milestones");
        if (milestones == null) {
            card.getChildren().add(new Label("Milestone data unavailable."));
            return card;
        }

        VBox list = new VBox(10);
        for (int i = 0; i < milestones.length(); i++) {
            JSONObject milestone = milestones.getJSONObject(i);
            list.getChildren().add(createMilestoneRow(milestone));
        }

        card.getChildren().add(list);
        return card;
    }

    private HBox createMilestoneRow(JSONObject milestone) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 6; -fx-border-color: #e0e0e0;");
        row.setAlignment(Pos.TOP_LEFT);

        Label weekLabel = new Label("Week " + milestone.optInt("week", 0));
        weekLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 80; -fx-text-fill: #3498db;");

        VBox details = new VBox(4);
        Label goal = new Label(milestone.optString("goal", "Goal"));
        goal.setStyle("-fx-font-weight: bold;");
        details.getChildren().add(goal);

        JSONArray metrics = milestone.optJSONArray("metrics");
        if (metrics != null) {
            for (int i = 0; i < metrics.length(); i++) {
                Label metric = new Label("◦ " + metrics.getString(i));
                metric.setStyle("-fx-text-fill: #666;");
                details.getChildren().add(metric);
            }
        }

        row.getChildren().addAll(weekLabel, details);
        return row;
    }

    private VBox createTargetsCard() {
        VBox card = createCard("Risk Reduction Targets");
        JSONObject targets = healthPlan.optJSONObject("risk_reduction_targets");
        if (targets == null || targets.isEmpty()) {
            card.getChildren().add(new Label("No risk reduction targets available."));
            return card;
        }

        VBox list = new VBox(10);
        for (String condition : targets.keySet()) {
            JSONObject target = targets.getJSONObject(condition);
            list.getChildren().add(createTargetRow(condition, target));
        }

        card.getChildren().add(list);
        return card;
    }

    private HBox createTargetRow(String condition, JSONObject target) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #e8f5e9; -fx-border-radius: 6;");
        row.setAlignment(Pos.CENTER_LEFT);

        Label conditionLabel = new Label(formatConditionName(condition));
        conditionLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 160;");

        String current = String.format("%.1f%%", target.optDouble("current_risk", 0));
        String goal = String.format("%.1f%%", target.optDouble("new_target_risk", 0));
        String reduction = String.format("-%.1f%%", target.optDouble("target_reduction", 0));

        Label currentLabel = new Label("Current: " + current);
        Label targetLabel = new Label("Target: " + goal);
        Label reductionLabel = new Label("Reduction: " + reduction);
        reductionLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        HBox stats = new HBox(15, currentLabel, targetLabel, reductionLabel);
        row.getChildren().addAll(conditionLabel, stats);
        return row;
    }

    private HBox createActionButtons() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Save Plan (JSON)");
        stylePrimaryButton(saveButton, "#3498db");
        saveButton.setOnAction(e -> savePlanAsJson());

        Button copyButton = new Button("Copy Plan");
        stylePrimaryButton(copyButton, "#2ecc71");
        copyButton.setOnAction(e -> copyPlanToClipboard());

        Button closeButton = new Button("Close");
        stylePrimaryButton(closeButton, "#64748b");
        closeButton.setOnAction(e -> {
            if (row.getScene() != null && row.getScene().getWindow() != null) {
                row.getScene().getWindow().hide();
            }
        });

        row.getChildren().addAll(saveButton, copyButton, closeButton);
        return row;
    }

    private void stylePrimaryButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18;");
    }

    private VBox createCard(String title) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-border-color: #e0e0e0;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        card.getChildren().add(titleLabel);
        return card;
    }

    private Label createTag(String text) {
        Label tag = new Label(text);
        tag.setPadding(new Insets(4, 10, 4, 10));
        tag.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-border-radius: 12; -fx-font-size: 11;");
        return tag;
    }

    private String formatConditionName(String condition) {
        switch (condition) {
            case "diabetes":
                return "Diabetes Risk";
            case "heart_disease":
                return "Heart Disease Risk";
            case "hypertension":
                return "Hypertension Risk";
            default:
                return condition.replace("_", " ").toUpperCase();
        }
    }

    private void savePlanAsJson() {
        Stage stage = (Stage) getScene().getWindow();
        if (stage == null) {
            showNotification("Unable to access window for saving. Please try again.", NotificationHelper.NotificationType.WARNING);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(healthPlan.toString(), new TypeReference<Map<String, Object>>() {});
            JSONExporter.exportToJSON(data, "health_plan", "Save Health Plan", stage);
        } catch (Exception e) {
            showNotification("Failed to save health plan: " + e.getMessage(), NotificationHelper.NotificationType.ERROR);
        }
    }

    private void copyPlanToClipboard() {
        ClipboardContent content = new ClipboardContent();
        content.putString(healthPlan.toString(2));
        Clipboard.getSystemClipboard().setContent(content);
        showNotification("Health plan copied to clipboard. Share it with your care team.", NotificationHelper.NotificationType.SUCCESS);
    }

    private void showNotification(String message, NotificationHelper.NotificationType type) {
        if (getScene() != null && getScene().getRoot() instanceof StackPane stackPane) {
            switch (type) {
                case SUCCESS -> NotificationHelper.showSuccessNotification(stackPane, message);
                case ERROR -> NotificationHelper.showErrorNotification(stackPane, message);
                case WARNING -> NotificationHelper.showWarningNotification(stackPane, message);
                default -> NotificationHelper.showInfoNotification(stackPane, message);
            }
        } else {
            Alert.AlertType alertType = switch (type) {
                case SUCCESS -> Alert.AlertType.INFORMATION;
                case WARNING -> Alert.AlertType.WARNING;
                case ERROR -> Alert.AlertType.ERROR;
                default -> Alert.AlertType.INFORMATION;
            };
            Alert alert = new Alert(alertType, message, ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public static void showPlanDialog(JSONObject plan) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Your Personalized Health Plan");
        dialog.setHeaderText("30-Day Action Plan Generated");

        HealthCoachPlanView view = new HealthCoachPlanView(plan);
        ScrollPane scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(1000);
        scrollPane.setPrefViewportHeight(680);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #f8f9fa;");

        dialog.showAndWait();
    }
}


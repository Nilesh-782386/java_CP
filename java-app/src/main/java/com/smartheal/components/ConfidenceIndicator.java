package com.smartheal.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

public class ConfidenceIndicator extends VBox {

    public ConfidenceIndicator(String diseaseName, JSONObject prediction) {
        super(8);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-background-radius: 6;");

        double risk = prediction.optDouble("risk_percentage", 0.0);
        JSONObject confidence = prediction.optJSONObject("confidence_interval");
        if (confidence == null) {
            confidence = new JSONObject()
                    .put("lower", 0.0)
                    .put("upper", 0.0)
                    .put("width", 0.0);
        }
        String category = prediction.optString("risk_category", "UNCERTAIN");
        double uncertainty = prediction.optDouble("uncertainty_score", 1.0);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label diseaseLabel = new Label(formatDiseaseName(diseaseName));
        diseaseLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Label categoryLabel = new Label("(" + category + ")");
        categoryLabel.setStyle(getCategoryStyle(category));

        header.getChildren().addAll(diseaseLabel, categoryLabel);

        Label riskLabel = new Label(String.format("Risk: %.1f%% (%.1f - %.1f%%)",
                risk,
                confidence.optDouble("lower", 0.0),
                confidence.optDouble("upper", 0.0)));
        riskLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; " + getRiskColor(risk));

        ProgressBar confidenceBar = new ProgressBar();
        double width = confidence.optDouble("width", 0.0) / 100.0;
        confidenceBar.setProgress(Math.min(Math.max(width, 0.0), 1.0));
        confidenceBar.setStyle("-fx-accent: " + getUncertaintyColor(uncertainty) + ";");
        confidenceBar.setPrefWidth(260);

        Label confidenceLabel = new Label(
                String.format("Confidence Interval Width: %.1f%%", confidence.optDouble("width", 0.0)));
        confidenceLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");

        getChildren().addAll(header, riskLabel, confidenceBar, confidenceLabel);
    }

    private String formatDiseaseName(String disease) {
        switch (disease) {
            case "diabetes":
                return "Diabetes Risk";
            case "heart_disease":
                return "Heart Disease Risk";
            case "hypertension":
                return "Hypertension Risk";
            default:
                return disease.replace("_", " ").toUpperCase();
        }
    }

    private String getCategoryStyle(String category) {
        switch (category) {
            case "LOW":
                return "-fx-text-fill: #28a745; -fx-font-weight: bold;";
            case "MODERATE":
                return "-fx-text-fill: #ffc107; -fx-font-weight: bold;";
            case "HIGH":
                return "-fx-text-fill: #fd7e14; -fx-font-weight: bold;";
            case "VERY_HIGH":
                return "-fx-text-fill: #dc3545; -fx-font-weight: bold;";
            case "UNCERTAIN":
            default:
                return "-fx-text-fill: #6c757d; -fx-font-weight: bold;";
        }
    }

    private String getRiskColor(double risk) {
        if (risk < 20) {
            return "-fx-text-fill: #28a745;";
        }
        if (risk < 50) {
            return "-fx-text-fill: #ffc107;";
        }
        if (risk < 80) {
            return "-fx-text-fill: #fd7e14;";
        }
        return "-fx-text-fill: #dc3545;";
    }

    private String getUncertaintyColor(double uncertainty) {
        if (uncertainty < 0.1) {
            return "#28a745";
        }
        if (uncertainty < 0.2) {
            return "#ffc107";
        }
        if (uncertainty < 0.3) {
            return "#fd7e14";
        }
        return "#dc3545";
    }
}


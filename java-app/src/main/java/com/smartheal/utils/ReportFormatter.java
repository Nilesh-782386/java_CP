package com.smartheal.utils;

import com.smartheal.models.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportFormatter {
    public static String formatSymptomAnalysis(List<SymptomCheckResult> results, List<String> selectedSymptoms) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("SMART HEALTH GUIDE+ - SYMPTOM ANALYSIS REPORT\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        
        sb.append("Selected Symptoms:\n");
        sb.append("-".repeat(60)).append("\n");
        for (int i = 0; i < selectedSymptoms.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, selectedSymptoms.get(i)));
        }
        sb.append("\n");
        
        sb.append("Analysis Results:\n");
        sb.append("=".repeat(60)).append("\n\n");
        
        for (int i = 0; i < results.size(); i++) {
            SymptomCheckResult result = results.get(i);
            Disease disease = result.getDisease();
            
            sb.append(String.format("Result #%d\n", i + 1));
            sb.append("-".repeat(60)).append("\n");
            sb.append(String.format("Condition: %s\n", disease.getName()));
            sb.append(String.format("Match Percentage: %.1f%%\n", result.getMatchPercentage()));
            sb.append(String.format("Severity: %s\n", disease.getSeverity().toUpperCase()));
            sb.append(String.format("\nDescription:\n%s\n", disease.getDescription()));
            sb.append(String.format("\nMatched Symptoms:\n"));
            for (String symptom : result.getMatchedSymptoms()) {
                sb.append(String.format("  • %s\n", symptom));
            }
            sb.append(String.format("\nCommon Treatments:\n"));
            for (String treatment : disease.getTreatments()) {
                sb.append(String.format("  • %s\n", treatment));
            }
            sb.append(String.format("\nWhen to Seek Medical Help:\n%s\n", disease.getWhenToSeekHelp()));
            sb.append("\n").append("=".repeat(60)).append("\n\n");
        }
        
        sb.append("\n⚠️ DISCLAIMER: This report is for educational purposes only.\n");
        sb.append("Always consult with a qualified healthcare provider for medical concerns.\n");
        
        return sb.toString();
    }

    public static String formatBloodReportAnalysis(ReportAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("SMART HEALTH GUIDE+ - BLOOD REPORT ANALYSIS\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        
        sb.append("Overall Status: ").append(analysis.getOverallStatus().toUpperCase()).append("\n");
        sb.append("Summary: ").append(analysis.getSummary()).append("\n\n");
        
        sb.append("Parameter Details:\n");
        sb.append("-".repeat(60)).append("\n");
        for (BloodParameter param : analysis.getParameters()) {
            sb.append(String.format("\n%s: %.2f %s\n", param.getName(), param.getValue(), param.getUnit()));
            sb.append(String.format("  Status: %s\n", param.getStatus().toUpperCase()));
            sb.append(String.format("  Normal Range: %.2f - %.2f %s\n", 
                param.getNormalMin(), param.getNormalMax(), param.getUnit()));
        }
        
        if (!analysis.getFlaggedParameters().isEmpty()) {
            sb.append("\nFlagged Parameters:\n");
            sb.append("-".repeat(60)).append("\n");
            for (String param : analysis.getFlaggedParameters()) {
                sb.append(String.format("  ⚠️ %s\n", param));
            }
        }
        
        if (!analysis.getRecommendations().isEmpty()) {
            sb.append("\nRecommendations:\n");
            sb.append("-".repeat(60)).append("\n");
            for (int i = 0; i < analysis.getRecommendations().size(); i++) {
                sb.append(String.format("%d. %s\n", i + 1, analysis.getRecommendations().get(i)));
            }
        }
        
        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("\n⚠️ DISCLAIMER: This report is for educational purposes only.\n");
        sb.append("Always consult with a qualified healthcare provider for medical concerns.\n");
        
        return sb.toString();
    }

    public static String formatCostEstimation(CostEstimation estimation) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("SMART HEALTH GUIDE+ - COST ESTIMATION REPORT\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        
        sb.append(String.format("Treatment: %s\n", estimation.getTreatmentName()));
        sb.append(String.format("Hospital Type: %s\n\n", estimation.getHospitalType()));
        
        sb.append("Cost Estimates:\n");
        sb.append("-".repeat(60)).append("\n");
        sb.append(String.format("Average Cost: ₹%.2f\n", estimation.getAverageCost()));
        sb.append(String.format("Minimum Cost: ₹%.2f\n", estimation.getMinCost()));
        sb.append(String.format("Maximum Cost: ₹%.2f\n\n", estimation.getMaxCost()));
        
        sb.append("Factors Affecting Cost:\n");
        sb.append("-".repeat(60)).append("\n");
        for (int i = 0; i < estimation.getFactors().size(); i++) {
            var factor = estimation.getFactors().get(i);
            sb.append(String.format("%d. %s\n", i + 1, factor.getName()));
            sb.append(String.format("   Impact: %s\n\n", factor.getImpact()));
        }
        
        sb.append("Disclaimer:\n");
        sb.append("-".repeat(60)).append("\n");
        sb.append(estimation.getDisclaimer()).append("\n");
        
        return sb.toString();
    }

    public static String formatRiskAssessment(com.smartheal.models.RiskAssessment assessment, 
                                             int age, double weight, double height,
                                             java.util.List<String> symptoms, java.util.List<String> familyHistory) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(70)).append("\n");
        sb.append("SMART HEALTH GUIDE+ - HEALTH RISK ASSESSMENT REPORT\n");
        sb.append("=".repeat(70)).append("\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        
        // Personal Information
        sb.append("Personal Information:\n");
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("Age: %d years\n", age));
        sb.append(String.format("Weight: %.1f kg\n", weight));
        sb.append(String.format("Height: %.1f cm\n", height));
        sb.append(String.format("BMI: %.1f (%s)\n", assessment.getBmi(), 
            assessment.getBmi() < 18.5 ? "Underweight" : 
            assessment.getBmi() < 25 ? "Normal" : 
            assessment.getBmi() < 30 ? "Overweight" : "Obese"));
        
        if (symptoms != null && !symptoms.isEmpty()) {
            sb.append("\nCurrent Symptoms:\n");
            for (String symptom : symptoms) {
                sb.append(String.format("  • %s\n", symptom));
            }
        }
        
        if (familyHistory != null && !familyHistory.isEmpty()) {
            sb.append("\nFamily History:\n");
            for (String history : familyHistory) {
                sb.append(String.format("  • %s\n", history));
            }
        }
        
        sb.append("\n");
        
        // Health Score
        if (assessment.getHealthScore() != null) {
            sb.append("Overall Health Score: ").append(assessment.getHealthScore()).append("/100\n");
            sb.append("-".repeat(70)).append("\n\n");
        }
        
        // Risk Assessments
        sb.append("Disease Risk Assessment:\n");
        sb.append("=".repeat(70)).append("\n\n");
        
        sb.append(String.format("Diabetes Risk: %d%%\n", assessment.getDiabetesRisk()));
        sb.append(String.format("  Level: %s\n", 
            assessment.getDiabetesRisk() >= 70 ? "HIGH" : 
            assessment.getDiabetesRisk() >= 50 ? "MODERATE" : "LOW"));
        
        sb.append(String.format("\nHeart Disease Risk: %d%%\n", assessment.getHeartRisk()));
        sb.append(String.format("  Level: %s\n", 
            assessment.getHeartRisk() >= 70 ? "HIGH" : 
            assessment.getHeartRisk() >= 50 ? "MODERATE" : "LOW"));
        
        sb.append(String.format("\nHypertension Risk: %d%%\n", assessment.getHypertensionRisk()));
        sb.append(String.format("  Level: %s\n", 
            assessment.getHypertensionRisk() >= 70 ? "HIGH" : 
            assessment.getHypertensionRisk() >= 50 ? "MODERATE" : "LOW"));
        
        // Estimated Values
        if (assessment.getEstimatedValues() != null && !assessment.getEstimatedValues().isEmpty()) {
            sb.append("\nEstimated Health Values:\n");
            sb.append("-".repeat(70)).append("\n");
            for (java.util.Map.Entry<String, Double> entry : assessment.getEstimatedValues().entrySet()) {
                String key = entry.getKey().replace("_", " ").substring(0, 1).toUpperCase() + 
                            entry.getKey().replace("_", " ").substring(1);
                String unit = key.contains("bp") ? " mmHg" : 
                             key.contains("glucose") ? " mg/dL" : 
                             key.contains("cholesterol") ? " mg/dL" : "";
                sb.append(String.format("%s: %.1f%s\n", key, entry.getValue(), unit));
            }
        }
        
        // Risk Trend
        if (assessment.getRiskTrend() != null && !assessment.getRiskTrend().isEmpty()) {
            sb.append("\n5-Year Risk Trend Prediction:\n");
            sb.append("-".repeat(70)).append("\n");
            for (java.util.Map.Entry<String, Object> entry : assessment.getRiskTrend().entrySet()) {
                if (entry.getValue() instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> trend = (java.util.Map<String, Object>) entry.getValue();
                    String disease = entry.getKey().replace("_", " ").substring(0, 1).toUpperCase() + 
                                   entry.getKey().replace("_", " ").substring(1);
                    sb.append(String.format("\n%s:\n", disease));
                    sb.append(String.format("  Current: %d%%\n", ((Number) trend.get("current")).intValue()));
                    sb.append(String.format("  Predicted (5 years): %d%%\n", ((Number) trend.get("predicted_5_years")).intValue()));
                    sb.append(String.format("  Trend: %s\n", ((String) trend.get("trend")).toUpperCase()));
                }
            }
        }
        
        // Recommendations
        if (assessment.getRecommendations() != null && !assessment.getRecommendations().isEmpty()) {
            sb.append("\n\nRecommendations:\n");
            sb.append("=".repeat(70)).append("\n");
            for (int i = 0; i < assessment.getRecommendations().size(); i++) {
                com.smartheal.models.Recommendation rec = assessment.getRecommendations().get(i);
                sb.append(String.format("\n%d. [%s PRIORITY - %s]\n", i + 1, 
                    rec.getPriority().toUpperCase(), rec.getCategory().toUpperCase()));
                sb.append(String.format("   %s\n", rec.getMessage()));
            }
        }
        
        // Action Plan
        if (assessment.getActionPlan() != null && !assessment.getActionPlan().isEmpty()) {
            sb.append("\n\nPersonalized Action Plan:\n");
            sb.append("=".repeat(70)).append("\n");
            for (java.util.Map<String, Object> action : assessment.getActionPlan()) {
                sb.append(String.format("\n#%d. %s [%s Impact]\n", 
                    action.get("priority"), action.get("title"), action.get("impact")));
                sb.append(String.format("   Category: %s\n", action.get("category")));
                sb.append(String.format("   Timeline: %s\n", action.get("timeline")));
                sb.append(String.format("   Description: %s\n", action.get("description")));
                
                @SuppressWarnings("unchecked")
                List<String> steps = (List<String>) action.get("steps");
                if (steps != null && !steps.isEmpty()) {
                    sb.append("   Steps:\n");
                    for (String step : steps) {
                        sb.append(String.format("     → %s\n", step));
                    }
                }
            }
        }
        
        // Screening Recommendations
        if (assessment.getScreeningRecommendations() != null && !assessment.getScreeningRecommendations().isEmpty()) {
            sb.append("\n\nRecommended Health Screenings:\n");
            sb.append("=".repeat(70)).append("\n");
            for (int i = 0; i < assessment.getScreeningRecommendations().size(); i++) {
                java.util.Map<String, Object> screening = assessment.getScreeningRecommendations().get(i);
                sb.append(String.format("\n%d. %s [%s Priority]\n", i + 1, 
                    screening.get("test"), ((String) screening.get("priority")).toUpperCase()));
                sb.append(String.format("   Frequency: %s\n", screening.get("frequency")));
                sb.append(String.format("   Purpose: %s\n", screening.get("purpose")));
            }
        }
        
        sb.append("\n").append("=".repeat(70)).append("\n");
        sb.append("\n⚠️ DISCLAIMER: This report is for educational purposes only.\n");
        sb.append("Always consult with a qualified healthcare provider for medical concerns.\n");
        sb.append("Risk assessments are estimates based on provided information and should not\n");
        sb.append("replace professional medical advice, diagnosis, or treatment.\n");
        
        return sb.toString();
    }
}


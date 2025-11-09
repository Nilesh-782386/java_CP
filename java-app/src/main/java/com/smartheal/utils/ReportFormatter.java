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
            sb.append(String.format("Symptom Coverage: %.1f%%\n", result.getSymptomCoverage()));

            SymptomConfidence confidence = result.getConfidence();
            if (confidence != null) {
                sb.append(String.format("Confidence Score: %.1f%% (%s)\n",
                    confidence.getScore() != null ? confidence.getScore() : result.getMatchPercentage(),
                    confidence.getLevel() != null ? confidence.getLevel() : "N/A"));
                if (confidence.getExplanation() != null) {
                    sb.append(String.format("Confidence Notes: %s\n", confidence.getExplanation()));
                }
            }

            SymptomTriage triage = result.getTriage();
            if (triage != null) {
                sb.append(String.format("Triage Recommendation: %s\n", triage.getLevel()));
                if (triage.getMessage() != null) {
                    sb.append(String.format("Triage Notes: %s\n", triage.getMessage()));
                }
                if (triage.getSpecialist() != null) {
                    sb.append(String.format("Recommended Specialist: %s\n", triage.getSpecialist()));
                }
            }

            sb.append(String.format("\nDescription:\n%s\n", disease.getDescription()));
            sb.append(String.format("\nMatched Symptoms:\n"));
            if (result.getMatchedSymptoms() != null && !result.getMatchedSymptoms().isEmpty()) {
                for (String symptom : result.getMatchedSymptoms()) {
                    sb.append(String.format("  • %s\n", symptom));
                }
            } else {
                sb.append("  • None reported\n");
            }
            if (result.getMissingSymptoms() != null && !result.getMissingSymptoms().isEmpty()) {
                sb.append(String.format("\nTypical Symptoms Not Reported:\n"));
                for (String symptom : result.getMissingSymptoms()) {
                    sb.append(String.format("  • %s\n", symptom));
                }
            }
            if (result.getCriticalSymptomsMissing() != null && !result.getCriticalSymptomsMissing().isEmpty()) {
                sb.append(String.format("\nKey Diagnostic Clues Missing:\n"));
                for (String symptom : result.getCriticalSymptomsMissing()) {
                    sb.append(String.format("  • %s\n", symptom));
                }
            }
            sb.append(String.format("\nCommon Treatments:\n"));
            if (disease.getTreatments() != null && !disease.getTreatments().isEmpty()) {
                for (String treatment : disease.getTreatments()) {
                    sb.append(String.format("  • %s\n", treatment));
                }
            } else {
                sb.append("  • Consult a healthcare professional for personalised treatment.\n");
            }

            if (result.getRecommendedTests() != null && !result.getRecommendedTests().isEmpty()) {
                sb.append(String.format("\nRecommended Tests & Investigations:\n"));
                for (String test : result.getRecommendedTests()) {
                    sb.append(String.format("  • %s\n", test));
                }
            }

            if (result.getRiskFactors() != null && !result.getRiskFactors().isEmpty()) {
                sb.append(String.format("\nCommon Risk Factors:\n"));
                for (String factor : result.getRiskFactors()) {
                    sb.append(String.format("  • %s\n", factor));
                }
            }

            if (result.getLifestyleAdvice() != null && !result.getLifestyleAdvice().isEmpty()) {
                sb.append(String.format("\nLifestyle & Self-care Guidance:\n"));
                for (String advice : result.getLifestyleAdvice()) {
                    sb.append(String.format("  • %s\n", advice));
                }
            }

            if (result.getMonitoringTips() != null && !result.getMonitoringTips().isEmpty()) {
                sb.append(String.format("\nHome Monitoring Tips:\n"));
                for (String tip : result.getMonitoringTips()) {
                    sb.append(String.format("  • %s\n", tip));
                }
            }

            if (result.getPrecautions() != null && !result.getPrecautions().isEmpty()) {
                sb.append(String.format("\nPrecautions & Follow-up:\n"));
                for (String precaution : result.getPrecautions()) {
                    sb.append(String.format("  • %s\n", precaution));
                }
            }

            if (result.getSimilarConditions() != null && !result.getSimilarConditions().isEmpty()) {
                sb.append(String.format("\nOther Possible Conditions to Discuss:\n"));
                for (SimilarCondition sc : result.getSimilarConditions()) {
                    if (sc.getName() != null) {
                        if (sc.getMatchPercentage() != null) {
                            sb.append(String.format("  • %s (%.1f%%)\n", sc.getName(), sc.getMatchPercentage()));
                        } else {
                            sb.append(String.format("  • %s\n", sc.getName()));
                        }
                    }
                }
            }

            if (result.getRedFlags() != null && !result.getRedFlags().isEmpty()) {
                sb.append(String.format("\n⚠️ Red Flag Symptoms Reported:\n"));
                for (String redFlag : result.getRedFlags()) {
                    sb.append(String.format("  • %s\n", redFlag));
                }
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
        return formatRiskAssessment(assessment, age, weight, height, symptoms, familyHistory, null, null);
    }

    @SuppressWarnings("unchecked")
    public static String formatRiskAssessment(com.smartheal.models.RiskAssessment assessment,
                                             int age, double weight, double height,
                                             java.util.List<String> symptoms, java.util.List<String> familyHistory,
                                             java.util.Map<String, Object> advancedInsights,
                                             java.util.Map<String, Object> lifestyleInputs) {
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

        if (lifestyleInputs != null && !lifestyleInputs.isEmpty()) {
            sb.append("\nLifestyle Profile Inputs Provided:\n");
            sb.append("-".repeat(70)).append("\n");
            String[] displayOrder = {
                "sleep_hours",
                "sleep_quality",
                "sleep_consistency",
                "snoring",
                "stress_level",
                "stress_coping",
                "mood_stability",
                "exercise",
                "moderate_activity_minutes",
                "vigorous_activity_minutes",
                "strength_training_sessions",
                "sedentary_hours",
                "diet_quality",
                "vegetable_servings",
                "processed_meals_per_week",
                "hydration_glasses",
                "caffeine_intake",
                "alcohol",
                "smoking",
                "smoking_intensity",
                "vaping",
                "environmental_exposure",
                "shift_work",
                "medication_adherence",
                "medication_side_effects",
                "last_checkup_months",
                "vaccination_status",
                "work_hours"
            };

            java.util.Set<String> printed = new java.util.LinkedHashSet<>();
            for (String key : displayOrder) {
                if (lifestyleInputs.containsKey(key)) {
                    Object value = lifestyleInputs.get(key);
                    sb.append(String.format("%s: %s\n", formatLifestyleLabel(key), formatLifestyleValue(key, value)));
                    printed.add(key);
                }
            }
            for (java.util.Map.Entry<String, Object> entry : lifestyleInputs.entrySet()) {
                if (!printed.contains(entry.getKey())) {
                    sb.append(String.format("%s: %s\n", formatLifestyleLabel(entry.getKey()), formatLifestyleValue(entry.getKey(), entry.getValue())));
                }
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

        if (advancedInsights != null && !advancedInsights.isEmpty()) {
            sb.append("\n\nAdvanced Lifestyle Intelligence:\n");
            sb.append("=".repeat(70)).append("\n");

            java.util.Map<String, Object> lifestyleSummary = (java.util.Map<String, Object>) advancedInsights.get("lifestyle_summary");
            if (lifestyleSummary != null && !lifestyleSummary.isEmpty()) {
                Number overallScore = (Number) lifestyleSummary.get("overall_score");
                String overallStatus = lifestyleSummary.get("overall_status") != null ? lifestyleSummary.get("overall_status").toString() : "Unknown";
                sb.append(String.format("Overall Lifestyle Score: %s%s\n",
                    overallScore != null ? String.format("%.1f", overallScore.doubleValue()) : "N/A",
                    overallScore != null ? "/100" : ""));
                sb.append(String.format("Status: %s\n", overallStatus));

                java.util.List<java.util.Map<String, Object>> priorityActions =
                    (java.util.List<java.util.Map<String, Object>>) lifestyleSummary.get("priority_actions");
                if (priorityActions != null && !priorityActions.isEmpty()) {
                    sb.append("\nTop Lifestyle Priorities:\n");
                    for (int i = 0; i < Math.min(priorityActions.size(), 5); i++) {
                        java.util.Map<String, Object> action = priorityActions.get(i);
                        sb.append(String.format("  %d. %s — %s (score %.0f)\n",
                            i + 1,
                            action.get("label"),
                            action.get("recommended_action"),
                            action.get("score") instanceof Number ? ((Number) action.get("score")).doubleValue() : 0.0));
                    }
                }

                java.util.List<java.util.Map<String, Object>> protective =
                    (java.util.List<java.util.Map<String, Object>>) lifestyleSummary.get("protective_factors");
                if (protective != null && !protective.isEmpty()) {
                    sb.append("\nProtective Strengths:\n");
                    for (int i = 0; i < Math.min(protective.size(), 5); i++) {
                        java.util.Map<String, Object> item = protective.get(i);
                        sb.append(String.format("  + %s — %s\n",
                            item.get("label"),
                            item.get("insight")));
                    }
                }

                java.util.Map<String, java.util.Map<String, Object>> categories =
                    (java.util.Map<String, java.util.Map<String, Object>>) lifestyleSummary.get("categories");
                if (categories != null && !categories.isEmpty()) {
                    sb.append("\nCategory Breakdown:\n");
                    java.util.List<java.util.Map.Entry<String, java.util.Map<String, Object>>> entries =
                        new java.util.ArrayList<>(categories.entrySet());
                    entries.sort(java.util.Comparator.comparing(e -> e.getValue().get("score") instanceof Number
                        ? -((Number) e.getValue().get("score")).doubleValue()
                        : -1.0));
                    for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : entries) {
                        java.util.Map<String, Object> cat = entry.getValue();
                        Number score = cat.get("score") instanceof Number ? (Number) cat.get("score") : null;
                        String status = cat.get("status") != null ? cat.get("status").toString() : "Needs attention";
                        sb.append(String.format("  • %s: %s%s (%s)\n",
                            cat.get("label"),
                            score != null ? String.format("%.0f", score.doubleValue()) : "N/A",
                            score != null ? "/100" : "",
                            status));
                        java.util.List<Object> risks = (java.util.List<Object>) cat.get("risks");
                        if (risks != null && !risks.isEmpty()) {
                            sb.append(String.format("      Focus: %s\n", risks.get(0)));
                        }
                        java.util.List<Object> positives = (java.util.List<Object>) cat.get("positives");
                        if (positives != null && !positives.isEmpty()) {
                            sb.append(String.format("      Strength: %s\n", positives.get(0)));
                        }
                    }
                }
            }

            java.util.Map<String, java.util.Map<String, Object>> diseaseImpacts =
                (java.util.Map<String, java.util.Map<String, Object>>) advancedInsights.get("disease_impacts");
            if (diseaseImpacts != null && !diseaseImpacts.isEmpty()) {
                sb.append("\nCondition-Specific Lifestyle Impact:\n");
                for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : diseaseImpacts.entrySet()) {
                    String disease = entry.getKey();
                    java.util.Map<String, Object> impact = entry.getValue();
                    Number adjustment = impact.get("adjustment") instanceof Number ? (Number) impact.get("adjustment") : null;
                    sb.append(String.format("  %s:\n", formatLifestyleLabel(disease)));
                    if (adjustment != null) {
                        sb.append(String.format("    Lifestyle adjustment applied: %+,.1f risk points\n", adjustment.doubleValue()));
                    }
                    java.util.List<Object> drivers = (java.util.List<Object>) impact.get("drivers");
                    if (drivers != null && !drivers.isEmpty()) {
                        sb.append("    Key risk drivers:\n");
                        for (Object driver : drivers) {
                            sb.append(String.format("      - %s\n", driver));
                        }
                    }
                    java.util.List<Object> protective = (java.util.List<Object>) impact.get("protective");
                    if (protective != null && !protective.isEmpty()) {
                        sb.append("    Protective strengths:\n");
                        for (Object item : protective) {
                            sb.append(String.format("      + %s\n", item));
                        }
                    }
                    if (impact.get("priority_action") != null) {
                        sb.append(String.format("    Suggested focus: %s\n", impact.get("priority_action")));
                    }
                }
            }

            java.util.Map<String, Object> dataQuality =
                (java.util.Map<String, Object>) advancedInsights.get("data_quality");
            if (dataQuality != null && !dataQuality.isEmpty()) {
                Number completeness = dataQuality.get("completeness") instanceof Number
                    ? (Number) dataQuality.get("completeness") : null;
                Number missing = dataQuality.get("missing_features") instanceof Number
                    ? (Number) dataQuality.get("missing_features") : null;
                Number total = dataQuality.get("total_features") instanceof Number
                    ? (Number) dataQuality.get("total_features") : null;
                sb.append("\nData Completeness:\n");
                sb.append(String.format("  Lifestyle datapoints provided: %d of %d (%.1f%%)\n",
                    total != null && missing != null ? total.intValue() - missing.intValue() : 0,
                    total != null ? total.intValue() : 0,
                    completeness != null ? completeness.doubleValue() : 0.0));
                if (missing != null && missing.intValue() > 0) {
                    sb.append("  Tip: Add remaining lifestyle details to further refine predictions.\n");
                }
            }
        }

        sb.append("\n").append("=".repeat(70)).append("\n");
        sb.append("\n⚠️ DISCLAIMER: This report is for educational purposes only.\n");
        sb.append("Always consult with a qualified healthcare provider for medical concerns.\n");
        sb.append("Risk assessments are estimates based on provided information and should not\n");
        sb.append("replace professional medical advice, diagnosis, or treatment.\n");

        return sb.toString();
    }

    private static String formatLifestyleLabel(String key) {
        if (key == null) {
            return "Value";
        }
        return switch (key) {
            case "vegetable_servings" -> "Fruit & vegetable servings/day";
            case "processed_meals_per_week" -> "Processed meals/week";
            case "hydration_glasses" -> "Water intake (glasses/day)";
            case "caffeine_intake" -> "Caffeine intake";
            case "moderate_activity_minutes" -> "Moderate activity (min/week)";
            case "vigorous_activity_minutes" -> "Vigorous activity (min/week)";
            case "strength_training_sessions" -> "Strength sessions/week";
            case "sedentary_hours" -> "Sitting time (hours/day)";
            case "sleep_hours" -> "Sleep hours/night";
            case "sleep_quality" -> "Sleep quality";
            case "sleep_consistency" -> "Sleep schedule consistency";
            case "snoring" -> "Snoring/apnea signs";
            case "stress_level" -> "Perceived stress level";
            case "stress_coping" -> "Stress coping ability";
            case "work_hours" -> "Work hours/week";
            case "mood_stability" -> "Mood stability";
            case "medication_adherence" -> "Medication adherence";
            case "medication_side_effects" -> "Medication side effects";
            case "last_checkup_months" -> "Months since last full check-up";
            case "vaccination_status" -> "Vaccination status";
            case "smoking_intensity" -> "Smoking intensity";
            case "smoking" -> "Smoking (yes/no)";
            case "vaping" -> "Vaping/e-cigarette use";
            case "environmental_exposure" -> "Work/environment exposure";
            case "shift_work" -> "Shift/night work";
            case "diet_quality" -> "Diet quality";
            case "exercise" -> "Exercise level";
            case "alcohol" -> "Alcohol intake";
            default -> key.replace("_", " ").substring(0, 1).toUpperCase() + key.replace("_", " ").substring(1);
        };
    }

    private static String formatLifestyleValue(String key, Object value) {
        if (value == null) {
            return "Not provided";
        }
        String[] caffeine = {"None", "Low (≤1 cup/day)", "Moderate (2 cups/day)", "High (3+ cups/day)"};
        String[] sleepQuality = {"Poor", "Fair", "Good", "Excellent"};
        String[] sleepConsistency = {"Irregular", "Somewhat regular", "Consistent", "Highly consistent"};
        String[] stressLevels = {"Low", "Moderate", "High"};
        String[] stressCoping = {"Struggling", "Managing", "Strong toolkit"};
        String[] moodStability = {"Low", "Variable", "Stable", "Very stable"};
        String[] medicationAdherence = {"Rarely follow plan", "Miss doses sometimes", "Mostly adherent", "Always on schedule"};
        String[] vaccinationStatus = {"Unsure / overdue", "Partially up to date", "Fully up to date"};
        String[] environmentExposure = {"Low exposure", "Moderate exposure", "High exposure"};
        String[] smokingIntensity = {"None", "Occasional (<5/day)", "Daily (5-10/day)", "Heavy (>10/day)"};
        String[] dietQuality = {"Poor", "Moderate", "Good"};
        String[] exerciseLevel = {"None", "Moderate", "High"};
        String[] alcoholLevel = {"None", "Moderate", "Heavy"};

        return switch (key) {
            case "caffeine_intake" -> mapOrdinal(value, caffeine);
            case "sleep_quality" -> mapOrdinal(value, sleepQuality);
            case "sleep_consistency" -> mapOrdinal(value, sleepConsistency);
            case "stress_level" -> mapOrdinal(value, stressLevels);
            case "stress_coping" -> mapOrdinal(value, stressCoping);
            case "mood_stability" -> mapOrdinal(value, moodStability);
            case "medication_adherence" -> mapOrdinal(value, medicationAdherence);
            case "vaccination_status" -> mapOrdinal(value, vaccinationStatus);
            case "environmental_exposure" -> mapOrdinal(value, environmentExposure);
            case "smoking_intensity" -> mapOrdinal(value, smokingIntensity);
            case "diet_quality" -> mapOrdinal(value, dietQuality);
            case "exercise" -> mapOrdinal(value, exerciseLevel);
            case "alcohol" -> mapOrdinal(value, alcoholLevel);
            case "snoring", "medication_side_effects", "vaping", "shift_work", "smoking" ->
                booleanFlag(value);
            default -> {
                if (value instanceof Number) {
                    double number = ((Number) value).doubleValue();
                    if (Math.abs(number - Math.rint(number)) < 1e-6) {
                        yield String.format("%.0f", number);
                    }
                    yield String.format("%.1f", number);
                }
                yield value.toString();
            }
        };
    }

    private static String mapOrdinal(Object value, String[] labels) {
        if (!(value instanceof Number) || labels.length == 0) {
            return labels.length > 0 ? labels[0] : "Not specified";
        }
        int index = ((Number) value).intValue();
        if (index < 0 || index >= labels.length) {
            index = Math.max(0, Math.min(labels.length - 1, index));
        }
        return labels[index];
    }

    private static String booleanFlag(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "Yes" : "No";
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 0 ? "No" : "Yes";
        }
        return "No";
    }
}


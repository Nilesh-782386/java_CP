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
}


package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SymptomCheckResult {
    private Disease disease;
    private double matchPercentage;  // Changed from int to double to match Python float
    private List<String> matchedSymptoms;
    private List<String> missingSymptoms;
    private List<String> criticalSymptomsMissing;
    private double symptomCoverage;
    private SymptomConfidence confidence;
    private SymptomTriage triage;
    private List<String> recommendedTests;
    private List<String> riskFactors;
    private List<String> lifestyleAdvice;
    private List<String> monitoringTips;
    private List<String> precautions;
    private List<SimilarCondition> similarConditions;
    private List<String> redFlags;

    public SymptomCheckResult() {}

    public Disease getDisease() {
        return disease;
    }

    public void setDisease(Disease disease) {
        this.disease = disease;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public List<String> getMatchedSymptoms() {
        return matchedSymptoms;
    }

    public void setMatchedSymptoms(List<String> matchedSymptoms) {
        this.matchedSymptoms = matchedSymptoms;
    }

    public List<String> getMissingSymptoms() {
        return missingSymptoms;
    }

    public void setMissingSymptoms(List<String> missingSymptoms) {
        this.missingSymptoms = missingSymptoms;
    }

    public List<String> getCriticalSymptomsMissing() {
        return criticalSymptomsMissing;
    }

    public void setCriticalSymptomsMissing(List<String> criticalSymptomsMissing) {
        this.criticalSymptomsMissing = criticalSymptomsMissing;
    }

    public double getSymptomCoverage() {
        return symptomCoverage;
    }

    public void setSymptomCoverage(double symptomCoverage) {
        this.symptomCoverage = symptomCoverage;
    }

    public SymptomConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(SymptomConfidence confidence) {
        this.confidence = confidence;
    }

    public SymptomTriage getTriage() {
        return triage;
    }

    public void setTriage(SymptomTriage triage) {
        this.triage = triage;
    }

    public List<String> getRecommendedTests() {
        return recommendedTests;
    }

    public void setRecommendedTests(List<String> recommendedTests) {
        this.recommendedTests = recommendedTests;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }

    public List<String> getLifestyleAdvice() {
        return lifestyleAdvice;
    }

    public void setLifestyleAdvice(List<String> lifestyleAdvice) {
        this.lifestyleAdvice = lifestyleAdvice;
    }

    public List<String> getMonitoringTips() {
        return monitoringTips;
    }

    public void setMonitoringTips(List<String> monitoringTips) {
        this.monitoringTips = monitoringTips;
    }

    public List<String> getPrecautions() {
        return precautions;
    }

    public void setPrecautions(List<String> precautions) {
        this.precautions = precautions;
    }

    public List<SimilarCondition> getSimilarConditions() {
        return similarConditions;
    }

    public void setSimilarConditions(List<SimilarCondition> similarConditions) {
        this.similarConditions = similarConditions;
    }

    public List<String> getRedFlags() {
        return redFlags;
    }

    public void setRedFlags(List<String> redFlags) {
        this.redFlags = redFlags;
    }
}


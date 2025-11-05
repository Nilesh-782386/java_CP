package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SymptomCheckResult {
    private Disease disease;
    private double matchPercentage;  // Changed from int to double to match Python float
    private List<String> matchedSymptoms;

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
}


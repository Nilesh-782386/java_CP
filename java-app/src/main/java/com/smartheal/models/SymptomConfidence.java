package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SymptomConfidence {
    private Double score;
    private String level;
    private Double modelProbability;
    private Double symptomCoverage;
    private String explanation;

    public SymptomConfidence() {}

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Double getModelProbability() {
        return modelProbability;
    }

    public void setModelProbability(Double modelProbability) {
        this.modelProbability = modelProbability;
    }

    public Double getSymptomCoverage() {
        return symptomCoverage;
    }

    public void setSymptomCoverage(Double symptomCoverage) {
        this.symptomCoverage = symptomCoverage;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}


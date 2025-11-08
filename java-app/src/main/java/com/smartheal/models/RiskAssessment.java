package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskAssessment {
    private int diabetesRisk;
    private int heartRisk;
    private int hypertensionRisk;
    private double bmi;
    private Integer healthScore;
    private List<Recommendation> recommendations;
    private Map<String, Double> estimatedValues;
    private Map<String, Object> riskTrend;
    private Map<String, Object> featureImportance;
    private Map<String, Object> riskReduction;
    private List<Map<String, Object>> actionPlan;
    private Map<String, Object> riskExplanations;
    private List<Map<String, Object>> screeningRecommendations;
    private Map<String, Object> populationComparison;

    public RiskAssessment() {}

    public int getDiabetesRisk() {
        return diabetesRisk;
    }

    public void setDiabetesRisk(int diabetesRisk) {
        this.diabetesRisk = diabetesRisk;
    }

    public int getHeartRisk() {
        return heartRisk;
    }

    public void setHeartRisk(int heartRisk) {
        this.heartRisk = heartRisk;
    }

    public int getHypertensionRisk() {
        return hypertensionRisk;
    }

    public void setHypertensionRisk(int hypertensionRisk) {
        this.hypertensionRisk = hypertensionRisk;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public Map<String, Double> getEstimatedValues() {
        return estimatedValues;
    }

    public void setEstimatedValues(Map<String, Double> estimatedValues) {
        this.estimatedValues = estimatedValues;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public Map<String, Object> getRiskTrend() {
        return riskTrend;
    }

    public void setRiskTrend(Map<String, Object> riskTrend) {
        this.riskTrend = riskTrend;
    }

    public Map<String, Object> getFeatureImportance() {
        return featureImportance;
    }

    public void setFeatureImportance(Map<String, Object> featureImportance) {
        this.featureImportance = featureImportance;
    }

    public Map<String, Object> getRiskReduction() {
        return riskReduction;
    }

    public void setRiskReduction(Map<String, Object> riskReduction) {
        this.riskReduction = riskReduction;
    }

    public List<Map<String, Object>> getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(List<Map<String, Object>> actionPlan) {
        this.actionPlan = actionPlan;
    }

    public Map<String, Object> getRiskExplanations() {
        return riskExplanations;
    }

    public void setRiskExplanations(Map<String, Object> riskExplanations) {
        this.riskExplanations = riskExplanations;
    }

    public List<Map<String, Object>> getScreeningRecommendations() {
        return screeningRecommendations;
    }

    public void setScreeningRecommendations(List<Map<String, Object>> screeningRecommendations) {
        this.screeningRecommendations = screeningRecommendations;
    }

    public Map<String, Object> getPopulationComparison() {
        return populationComparison;
    }

    public void setPopulationComparison(Map<String, Object> populationComparison) {
        this.populationComparison = populationComparison;
    }
}


package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRecommendation {
    private String diseaseId;
    private String diseaseName;
    private List<MedicalTest> recommendedTests;
    private List<MedicalTest> testsToAvoid;
    private String reasoning;

    public TestRecommendation() {}

    public String getDiseaseId() {
        return diseaseId;
    }

    public void setDiseaseId(String diseaseId) {
        this.diseaseId = diseaseId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public List<MedicalTest> getRecommendedTests() {
        return recommendedTests;
    }

    public void setRecommendedTests(List<MedicalTest> recommendedTests) {
        this.recommendedTests = recommendedTests;
    }

    public List<MedicalTest> getTestsToAvoid() {
        return testsToAvoid;
    }

    public void setTestsToAvoid(List<MedicalTest> testsToAvoid) {
        this.testsToAvoid = testsToAvoid;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}


package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportAnalysis {
    private List<BloodParameter> parameters;
    private String overallStatus;
    private String summary;
    private List<String> recommendations;
    private List<String> flaggedParameters;

    public ReportAnalysis() {}

    public List<BloodParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<BloodParameter> parameters) {
        this.parameters = parameters;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getFlaggedParameters() {
        return flaggedParameters;
    }

    public void setFlaggedParameters(List<String> flaggedParameters) {
        this.flaggedParameters = flaggedParameters;
    }
}


package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Disease {
    private String id;
    private String name;
    private String description;
    private String severity;
    private List<String> symptoms;  // Changed from commonSymptoms to symptoms to match Python
    private List<String> commonSymptoms;  // Keep for backward compatibility
    private List<String> treatments;
    private String whenToSeekHelp;

    public Disease() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    // Get symptoms - check both 'symptoms' and 'commonSymptoms' for compatibility
    public List<String> getSymptoms() {
        if (symptoms != null && !symptoms.isEmpty()) {
            return symptoms;
        }
        return commonSymptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
        this.commonSymptoms = symptoms;  // Keep in sync
    }

    public List<String> getCommonSymptoms() {
        if (commonSymptoms != null && !commonSymptoms.isEmpty()) {
            return commonSymptoms;
        }
        return symptoms;
    }

    public void setCommonSymptoms(List<String> commonSymptoms) {
        this.commonSymptoms = commonSymptoms;
        this.symptoms = commonSymptoms;  // Keep in sync
    }

    public List<String> getTreatments() {
        return treatments;
    }

    public void setTreatments(List<String> treatments) {
        this.treatments = treatments;
    }

    public String getWhenToSeekHelp() {
        return whenToSeekHelp;
    }

    public void setWhenToSeekHelp(String whenToSeekHelp) {
        this.whenToSeekHelp = whenToSeekHelp;
    }
}


package com.smartheal.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {
    private String response;
    private List<String> relatedTopics;
    private Double confidence; // ML confidence score (0.0 to 1.0)

    public ChatResponse() {}

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<String> getRelatedTopics() {
        return relatedTopics;
    }

    public void setRelatedTopics(List<String> relatedTopics) {
        this.relatedTopics = relatedTopics;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}


package com.smartheal.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartheal.models.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:5000/api";
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 10000; // 10 seconds
    
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final PoolingHttpClientConnectionManager connectionManager;
    
    // Cache for static data
    private List<Symptom> cachedSymptoms;
    private List<Disease> cachedDiseases;
    private List<String> cachedTreatments;

    public ApiClient() {
        this.objectMapper = new ObjectMapper();
        
        // Configure connection pooling
        this.connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(10);
        
        // Configure request timeouts
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.of(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS))
            .setResponseTimeout(Timeout.of(READ_TIMEOUT, TimeUnit.MILLISECONDS))
            .build();
        
        this.httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .evictIdleConnections(Timeout.of(30, TimeUnit.SECONDS))
            .evictExpiredConnections()
            .build();
    }

    // Get all symptoms (with caching)
    public List<Symptom> getSymptoms() throws IOException {
        if (cachedSymptoms != null) {
            return cachedSymptoms;
        }
        HttpGet request = new HttpGet(BASE_URL + "/symptoms");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (statusCode >= 400) {
                throw new IOException("API Error [" + statusCode + "]: " + json);
            }
            cachedSymptoms = objectMapper.readValue(json, new TypeReference<List<Symptom>>() {});
            return cachedSymptoms;
        }
    }

    // Get all diseases (with caching)
    public List<Disease> getDiseases() throws IOException {
        if (cachedDiseases != null) {
            return cachedDiseases;
        }
        HttpGet request = new HttpGet(BASE_URL + "/diseases");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (statusCode >= 400) {
                throw new IOException("API Error [" + statusCode + "]: " + json);
            }
            cachedDiseases = objectMapper.readValue(json, new TypeReference<List<Disease>>() {});
            return cachedDiseases;
        }
    }

    // Get all treatments (with caching)
    public List<String> getTreatments() throws IOException {
        if (cachedTreatments != null) {
            return cachedTreatments;
        }
        HttpGet request = new HttpGet(BASE_URL + "/treatments");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (statusCode >= 400) {
                throw new IOException("API Error [" + statusCode + "]: " + json);
            }
            cachedTreatments = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return cachedTreatments;
        }
    }
    
    // Clear cache (useful for testing or refresh)
    public void clearCache() {
        cachedSymptoms = null;
        cachedDiseases = null;
        cachedTreatments = null;
    }

    // Check symptoms
    public List<SymptomCheckResult> checkSymptoms(List<String> symptomIds) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/check-symptoms");
        Map<String, Object> body = new HashMap<>();
        body.put("symptomIds", symptomIds);
        String jsonBody = objectMapper.writeValueAsString(body);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
                System.out.println("API Response: " + json);  // Debug log
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            
            int statusCode = response.getCode();
            if (statusCode >= 400) {
                System.err.println("API Error [" + statusCode + "]: " + json);
                throw new IOException("API Error [" + statusCode + "]: " + json);
            }
            
            try {
                List<SymptomCheckResult> results = objectMapper.readValue(json, new TypeReference<List<SymptomCheckResult>>() {});
                System.out.println("Parsed " + (results != null ? results.size() : 0) + " results from JSON");
                return results;
            } catch (Exception e) {
                System.err.println("Failed to parse JSON response: " + e.getMessage());
                System.err.println("Response JSON: " + json);
                throw new IOException("Failed to parse JSON response: " + e.getMessage() + "\nResponse: " + json, e);
            }
        }
    }

    // Chat
    public ChatResponse chat(String message) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/chat");
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        String jsonBody = objectMapper.writeValueAsString(body);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (response.getCode() >= 400) {
                throw new IOException("API Error: " + json);
            }
            return objectMapper.readValue(json, ChatResponse.class);
        }
    }

    // Estimate cost
    public CostEstimation estimateCost(String treatmentType, String hospitalType) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/estimate-cost");
        Map<String, String> body = new HashMap<>();
        body.put("treatmentType", treatmentType);
        body.put("hospitalType", hospitalType);
        String jsonBody = objectMapper.writeValueAsString(body);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (response.getCode() >= 400) {
                throw new IOException("API Error: " + json);
            }
            return objectMapper.readValue(json, CostEstimation.class);
        }
    }

    // Analyze report
    public ReportAnalysis analyzeReport(Map<String, Double> reportData) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/analyze-report");
        String jsonBody = objectMapper.writeValueAsString(reportData);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (response.getCode() >= 400) {
                throw new IOException("API Error: " + json);
            }
            return objectMapper.readValue(json, ReportAnalysis.class);
        }
    }
    
    // Risk assessment (Enhanced with new features)
    public com.smartheal.models.RiskAssessment assessRisk(
            int age, double weight, double height,
            java.util.List<String> symptoms,
            java.util.List<String> familyHistory,
            boolean smoking, int exercise, int alcohol,
            Double sleepHours, Integer stressLevel, Integer dietQuality) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/risk-assessment");
        Map<String, Object> body = new HashMap<>();
        body.put("age", age);
        body.put("weight", weight);
        body.put("height", height);
        body.put("symptoms", symptoms != null ? symptoms : new java.util.ArrayList<>());
        body.put("family_history", familyHistory != null ? familyHistory : new java.util.ArrayList<>());
        body.put("smoking", smoking);
        body.put("exercise", exercise);
        body.put("alcohol", alcohol);
        if (sleepHours != null) {
            body.put("sleep_hours", sleepHours);
        }
        if (stressLevel != null) {
            body.put("stress_level", stressLevel);
        }
        if (dietQuality != null) {
            body.put("diet_quality", dietQuality);
        }
        String jsonBody = objectMapper.writeValueAsString(body);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (response.getCode() >= 400) {
                throw new IOException("API Error: " + json);
            }
            return objectMapper.readValue(json, com.smartheal.models.RiskAssessment.class);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadReportImage(String base64Image) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/upload-report-image");
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("image", base64Image);
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (org.apache.hc.core5.http.ParseException e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (response.getCode() >= 400) {
                throw new IOException("API Error: " + json);
            }
            return objectMapper.readValue(json, Map.class);
        }
    }

    public Map<String, Object> parseReportText(String text) throws IOException {
        HttpPost request = new HttpPost(BASE_URL + "/parse-report-text");
        request.setHeader("Content-Type", "application/json");
        
        Map<String, String> payload = new HashMap<>();
        payload.put("text", text);
        
        String jsonPayload = objectMapper.writeValueAsString(payload);
        request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (org.apache.hc.core5.http.ParseException e) {
                throw new IOException("Failed to parse response: " + e.getMessage(), e);
            }
            if (response.getCode() >= 400) {
                throw new IOException("API Error: " + json);
            }
            return objectMapper.readValue(json, Map.class);
        }
    }
    
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
        if (connectionManager != null) {
            connectionManager.close();
        }
    }
    
    // Check if backend is available with better error handling
    public boolean isBackendAvailable() {
        try {
            HttpGet request = new HttpGet(BASE_URL + "/symptoms");
            request.setConfig(RequestConfig.custom()
                .setConnectTimeout(Timeout.of(3, TimeUnit.SECONDS))
                .setResponseTimeout(Timeout.of(3, TimeUnit.SECONDS))
                .build());
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                boolean available = statusCode == 200;
                if (!available) {
                    System.err.println("Backend responded with status code: " + statusCode);
                }
                return available;
            }
        } catch (java.net.ConnectException e) {
            System.err.println("Backend connection failed: Cannot connect to " + BASE_URL);
            System.err.println("  Make sure the Python backend server is running on port 5000");
            return false;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Backend connection timeout: Server did not respond within 3 seconds");
            return false;
        } catch (Exception e) {
            System.err.println("Backend availability check failed: " + e.getMessage());
            return false;
        }
    }
    
    // Enhanced connection test with detailed logging
    public String testConnection() {
        try {
            HttpGet request = new HttpGet(BASE_URL + "/symptoms");
            request.setConfig(RequestConfig.custom()
                .setConnectTimeout(Timeout.of(3, TimeUnit.SECONDS))
                .setResponseTimeout(Timeout.of(3, TimeUnit.SECONDS))
                .build());
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    return "Connected successfully";
                } else {
                    return "Backend responded with status: " + statusCode;
                }
            }
        } catch (java.net.ConnectException e) {
            return "Connection refused - Python backend not running on port 5000";
        } catch (java.net.SocketTimeoutException e) {
            return "Connection timeout - Backend not responding";
        } catch (Exception e) {
            return "Connection test failed: " + e.getMessage();
        }
    }
}


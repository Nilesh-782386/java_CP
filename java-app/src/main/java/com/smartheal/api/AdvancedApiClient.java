package com.smartheal.api;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.IOException;

public class AdvancedApiClient {
    private static final String BASE_URL = "http://localhost:5000/api";

    private AdvancedApiClient() {
    }

    public static JSONObject getAdvancedRiskAssessment(JSONObject userData) {
        HttpPost request = new HttpPost(BASE_URL + "/advanced-risk-assessment");
        request.setEntity(new StringEntity(userData.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                throw new IOException("Failed to read advanced assessment response: " + e.getMessage(), e);
            }

            if (response.getCode() >= 400) {
                throw new IOException("Advanced assessment API error: " + json);
            }
            return new JSONObject(json);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}


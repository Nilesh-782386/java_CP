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

public class HealthCoachApiClient {
    private static final String BASE_URL = "http://localhost:5000/api";

    private HealthCoachApiClient() {
    }

    public static JSONObject generatePlan(JSONObject payload) {
        HttpPost request = new HttpPost(BASE_URL + "/health-coach-plan");
        request.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            String json;
            try {
                json = EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                throw new IOException("Failed to read health coach response: " + e.getMessage(), e);
            }

            if (response.getCode() >= 400) {
                throw new IOException("Health coach API error: " + json);
            }

            return new JSONObject(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


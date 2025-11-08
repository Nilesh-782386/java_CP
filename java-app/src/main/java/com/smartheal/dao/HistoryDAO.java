package com.smartheal.dao;

import com.smartheal.database.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryDAO {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Save symptom analysis history
     */
    public boolean saveSymptomHistory(int userId, String selectedSymptoms, String predictedConditions, String analysisResultJson) {
        String sql = "INSERT INTO symptom_history (user_id, selected_symptoms, predicted_conditions, analysis_result) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, selectedSymptoms);
            stmt.setString(3, predictedConditions);
            stmt.setString(4, analysisResultJson);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving symptom history: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get symptom history for user
     */
    public List<Map<String, Object>> getSymptomHistory(int userId, int limit) {
        String sql = "SELECT * FROM symptom_history WHERE user_id = ? ORDER BY analysis_date DESC LIMIT ?";
        List<Map<String, Object>> history = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getInt("id"));
                    record.put("selectedSymptoms", rs.getString("selected_symptoms"));
                    record.put("predictedConditions", rs.getString("predicted_conditions"));
                    record.put("analysisDate", rs.getTimestamp("analysis_date"));
                    record.put("analysisResult", rs.getString("analysis_result"));
                    history.add(record);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting symptom history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Save chat history
     */
    public boolean saveChatHistory(int userId, String userMessage, String botResponse) {
        String sql = "INSERT INTO chat_history (user_id, user_message, bot_response) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, userMessage);
            stmt.setString(3, botResponse);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving chat history: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get chat history for user
     */
    public List<Map<String, Object>> getChatHistory(int userId, int limit) {
        String sql = "SELECT * FROM chat_history WHERE user_id = ? ORDER BY chat_date DESC LIMIT ?";
        List<Map<String, Object>> history = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getInt("id"));
                    record.put("userMessage", rs.getString("user_message"));
                    record.put("botResponse", rs.getString("bot_response"));
                    record.put("chatDate", rs.getTimestamp("chat_date"));
                    history.add(record);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting chat history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Save report analysis history
     */
    public boolean saveReportHistory(int userId, String reportDataJson, String analysisResult, String flaggedParameters, String overallStatus) {
        String sql = "INSERT INTO report_history (user_id, report_data, analysis_result, flagged_parameters, overall_status) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, reportDataJson);
            stmt.setString(3, analysisResult);
            stmt.setString(4, flaggedParameters);
            stmt.setString(5, overallStatus);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving report history: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get report history for user
     */
    public List<Map<String, Object>> getReportHistory(int userId, int limit) {
        String sql = "SELECT * FROM report_history WHERE user_id = ? ORDER BY analysis_date DESC LIMIT ?";
        List<Map<String, Object>> history = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getInt("id"));
                    record.put("reportData", rs.getString("report_data"));
                    record.put("analysisResult", rs.getString("analysis_result"));
                    record.put("flaggedParameters", rs.getString("flagged_parameters"));
                    record.put("overallStatus", rs.getString("overall_status"));
                    record.put("analysisDate", rs.getTimestamp("analysis_date"));
                    history.add(record);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting report history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Save cost estimation history
     */
    public boolean saveCostEstimationHistory(int userId, String treatmentType, String hospitalType, 
                                            double estimatedCost, double minCost, double maxCost) {
        String sql = "INSERT INTO cost_estimation_history (user_id, treatment_type, hospital_type, estimated_cost, min_cost, max_cost) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, treatmentType);
            stmt.setString(3, hospitalType);
            stmt.setDouble(4, estimatedCost);
            stmt.setDouble(5, minCost);
            stmt.setDouble(6, maxCost);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving cost estimation history: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get cost estimation history for user
     */
    public List<Map<String, Object>> getCostEstimationHistory(int userId, int limit) {
        String sql = "SELECT * FROM cost_estimation_history WHERE user_id = ? ORDER BY estimation_date DESC LIMIT ?";
        List<Map<String, Object>> history = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getInt("id"));
                    record.put("treatmentType", rs.getString("treatment_type"));
                    record.put("hospitalType", rs.getString("hospital_type"));
                    record.put("estimatedCost", rs.getDouble("estimated_cost"));
                    record.put("minCost", rs.getDouble("min_cost"));
                    record.put("maxCost", rs.getDouble("max_cost"));
                    record.put("estimationDate", rs.getTimestamp("estimation_date"));
                    history.add(record);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting cost estimation history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
    
    /**
     * Save risk assessment history
     */
    public boolean saveRiskHistory(int userId, int age, double weight, double height, String assessmentResultJson) {
        String sql = "INSERT INTO risk_history (user_id, age, weight, height, assessment_result) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, age);
            stmt.setDouble(3, weight);
            stmt.setDouble(4, height);
            stmt.setString(5, assessmentResultJson);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving risk history: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get risk assessment history for user
     */
    public List<Map<String, Object>> getRiskHistory(int userId, int limit) {
        String sql = "SELECT * FROM risk_history WHERE user_id = ? ORDER BY assessment_date DESC LIMIT ?";
        List<Map<String, Object>> history = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getInt("id"));
                    record.put("age", rs.getInt("age"));
                    record.put("weight", rs.getDouble("weight"));
                    record.put("height", rs.getDouble("height"));
                    record.put("assessmentResult", rs.getString("assessment_result"));
                    record.put("assessmentDate", rs.getTimestamp("assessment_date"));
                    history.add(record);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting risk history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return history;
    }
}


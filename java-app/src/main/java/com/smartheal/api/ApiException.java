package com.smartheal.api;

/**
 * Custom exception for API-related errors
 */
public class ApiException extends Exception {
    private final int statusCode;
    
    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
    }
    
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }
    
    public boolean isServerError() {
        return statusCode >= 500;
    }
}


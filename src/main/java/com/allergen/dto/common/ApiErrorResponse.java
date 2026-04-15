package com.allergen.dto.common;

import java.time.Instant;
import java.util.Map;

public class ApiErrorResponse {

    private String error;
    private String timestamp;
    private Map<String, String> fieldErrors;

    public ApiErrorResponse() {
        this.timestamp = Instant.now().toString();
    }

    public ApiErrorResponse(String error) {
        this.error = error;
        this.timestamp = Instant.now().toString();
    }

    public ApiErrorResponse(String error, Map<String, String> fieldErrors) {
        this.error = error;
        this.fieldErrors = fieldErrors;
        this.timestamp = Instant.now().toString();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}




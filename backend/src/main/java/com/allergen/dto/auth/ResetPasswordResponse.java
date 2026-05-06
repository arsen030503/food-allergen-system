package com.allergen.dto.auth;

public class ResetPasswordResponse {

    private String temporaryPassword;
    private String message;

    public ResetPasswordResponse() {}

    public ResetPasswordResponse(String message, String temporaryPassword) {
        this.message = message;
        this.temporaryPassword = temporaryPassword;
    }

    public String getTemporaryPassword() { return temporaryPassword; }
    public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

package com.allergen.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "{validation.auth.currentPassword.required}")
    private String currentPassword;

    @NotBlank(message = "{validation.auth.newPassword.required}")
    @Size(min = 8, max = 128, message = "{validation.auth.newPassword.size}")
    private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

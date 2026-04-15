package com.allergen.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class UpdateNameRequest {

    @NotBlank(message = "Name cannot be empty")
    private String fullName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}



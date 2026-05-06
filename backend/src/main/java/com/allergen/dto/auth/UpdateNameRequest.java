package com.allergen.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class UpdateNameRequest {

    @NotBlank(message = "{validation.auth.name.required}")
    private String fullName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}



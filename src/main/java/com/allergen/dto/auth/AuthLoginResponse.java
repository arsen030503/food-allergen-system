package com.allergen.dto.auth;

public class AuthLoginResponse {

    private String message;
    private Long userId;
    private String fullName;
    private String email;
    private String myAllergens;
    private String role;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMyAllergens() {
        return myAllergens;
    }

    public void setMyAllergens(String myAllergens) {
        this.myAllergens = myAllergens;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

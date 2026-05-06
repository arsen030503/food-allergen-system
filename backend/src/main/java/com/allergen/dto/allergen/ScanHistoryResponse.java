package com.allergen.dto.allergen;

import java.time.LocalDateTime;

public class ScanHistoryResponse {

    private Long id;
    private String ingredients;
    private LocalDateTime scannedAt;
    private int allergenCount;
    private String productName;
    /** Comma-separated detected allergen names (as stored). */
    private String detectedAllergens;
    /** How many detected allergens intersect the user's current profile. */
    private int profileMatchCount;
    /** True when the user has no profile configured, or no profile allergens matched this scan. */
    private boolean safeForUser;
    private boolean userProfileConfigured;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public int getAllergenCount() {
        return allergenCount;
    }

    public void setAllergenCount(int allergenCount) {
        this.allergenCount = allergenCount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDetectedAllergens() {
        return detectedAllergens;
    }

    public void setDetectedAllergens(String detectedAllergens) {
        this.detectedAllergens = detectedAllergens;
    }

    public int getProfileMatchCount() {
        return profileMatchCount;
    }

    public void setProfileMatchCount(int profileMatchCount) {
        this.profileMatchCount = profileMatchCount;
    }

    public boolean isSafeForUser() {
        return safeForUser;
    }

    public void setSafeForUser(boolean safeForUser) {
        this.safeForUser = safeForUser;
    }

    public boolean isUserProfileConfigured() {
        return userProfileConfigured;
    }

    public void setUserProfileConfigured(boolean userProfileConfigured) {
        this.userProfileConfigured = userProfileConfigured;
    }
}

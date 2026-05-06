package com.allergen.dto.allergen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalyzeResponse {

    private List<Map<String, String>> detectedAllergens = new ArrayList<>();
    /** Subset of {@link #detectedAllergens} that intersect the user's saved allergen profile. */
    private List<Map<String, String>> matchesForUser = new ArrayList<>();
    private int totalFound;
    /** True when no allergens were detected in the product at all. */
    private boolean safe;
    /** True when the user has configured at least one profile allergen and {@link #matchesForUser} is empty. */
    private boolean safeForUser;
    private boolean userProfileConfigured;

    public List<Map<String, String>> getDetectedAllergens() {
        return detectedAllergens;
    }

    public void setDetectedAllergens(List<Map<String, String>> detectedAllergens) {
        this.detectedAllergens = detectedAllergens;
    }

    public List<Map<String, String>> getMatchesForUser() {
        return matchesForUser;
    }

    public void setMatchesForUser(List<Map<String, String>> matchesForUser) {
        this.matchesForUser = matchesForUser;
    }

    public int getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(int totalFound) {
        this.totalFound = totalFound;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
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



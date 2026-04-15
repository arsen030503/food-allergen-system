package com.allergen.dto.allergen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalyzeResponse {

    private List<Map<String, String>> detectedAllergens = new ArrayList<>();
    private int totalFound;
    private boolean safe;

    public List<Map<String, String>> getDetectedAllergens() {
        return detectedAllergens;
    }

    public void setDetectedAllergens(List<Map<String, String>> detectedAllergens) {
        this.detectedAllergens = detectedAllergens;
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
}



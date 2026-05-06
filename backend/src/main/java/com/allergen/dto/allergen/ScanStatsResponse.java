package com.allergen.dto.allergen;

public class ScanStatsResponse {

    private int totalScans;
    private int safeScans;
    private int userAllergenCount;

    public int getTotalScans() {
        return totalScans;
    }

    public void setTotalScans(int totalScans) {
        this.totalScans = totalScans;
    }

    public int getSafeScans() {
        return safeScans;
    }

    public void setSafeScans(int safeScans) {
        this.safeScans = safeScans;
    }

    public int getUserAllergenCount() {
        return userAllergenCount;
    }

    public void setUserAllergenCount(int userAllergenCount) {
        this.userAllergenCount = userAllergenCount;
    }
}

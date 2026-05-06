package com.allergen.dto.auth;

public class UserProfileResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String myAllergens;
    /** ISO-8601 local time string */
    private String createdAt;
    /** JPEG data URL; same as {@link #avatarThumbData} for backward compatibility */
    private String avatarData;
    /** JPEG data URL, small (thumbnail) */
    private String avatarThumbData;
    /** JPEG data URL, compressed full-size (may be omitted in admin list) */
    private String avatarFullData;
    private String role;
    private boolean blocked;

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAvatarData() {
        return avatarData;
    }

    public void setAvatarData(String avatarData) {
        this.avatarData = avatarData;
    }

    public String getAvatarThumbData() {
        return avatarThumbData;
    }

    public void setAvatarThumbData(String avatarThumbData) {
        this.avatarThumbData = avatarThumbData;
    }

    public String getAvatarFullData() {
        return avatarFullData;
    }

    public void setAvatarFullData(String avatarFullData) {
        this.avatarFullData = avatarFullData;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}

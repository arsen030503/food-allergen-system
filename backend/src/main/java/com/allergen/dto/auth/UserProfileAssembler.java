package com.allergen.dto.auth;

import com.allergen.model.User;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Component
public class UserProfileAssembler {

    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String JPEG_DATA_PREFIX = "data:image/jpeg;base64,";

    public UserProfileResponse toResponse(User user, boolean includeFullAvatar) {
        UserProfileResponse r = new UserProfileResponse();
        r.setUserId(user.getId());
        r.setFullName(user.getFullName());
        r.setEmail(user.getEmail());
        r.setMyAllergens(user.getMyAllergens());
        if (user.getCreatedAt() != null) {
            r.setCreatedAt(user.getCreatedAt().format(ISO_LOCAL));
        }
        r.setRole(user.getRole().name());
        r.setBlocked(user.getBlockedAt() != null);

        if (user.getAvatarThumb() != null && user.getAvatarThumb().length > 0) {
            String thumb = JPEG_DATA_PREFIX + Base64.getEncoder().encodeToString(user.getAvatarThumb());
            r.setAvatarThumbData(thumb);
            r.setAvatarData(thumb);
        }
        if (includeFullAvatar && user.getAvatarFull() != null && user.getAvatarFull().length > 0) {
            r.setAvatarFullData(JPEG_DATA_PREFIX + Base64.getEncoder().encodeToString(user.getAvatarFull()));
        }
        return r;
    }
}

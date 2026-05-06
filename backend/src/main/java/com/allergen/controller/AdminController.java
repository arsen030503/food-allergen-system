package com.allergen.controller;

import com.allergen.dto.allergen.ScanHistoryResponse;
import com.allergen.dto.auth.ResetPasswordResponse;
import com.allergen.dto.auth.UpdateAllergensRequest;
import com.allergen.dto.auth.UpdateNameRequest;
import com.allergen.dto.auth.UpdateNameResponse;
import com.allergen.dto.auth.UserBlockRequest;
import com.allergen.dto.auth.UserProfileAssembler;
import com.allergen.dto.auth.UserProfileResponse;
import com.allergen.dto.common.MessageResponse;
import com.allergen.exception.ForbiddenException;
import com.allergen.exception.UnauthorizedException;
import com.allergen.model.Role;
import com.allergen.model.User;
import com.allergen.repository.UserRepository;
import com.allergen.security.JwtService;
import com.allergen.service.AllergenService;
import com.allergen.util.SecurePasswordGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserProfileAssembler userProfileAssembler;
    private final AllergenService allergenService;
    private final SecurePasswordGenerator passwordGenerator;
    private final MessageSource messageSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AdminController(UserRepository userRepository,
                           JwtService jwtService,
                           UserProfileAssembler userProfileAssembler,
                           AllergenService allergenService,
                           SecurePasswordGenerator passwordGenerator,
                           MessageSource messageSource) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userProfileAssembler = userProfileAssembler;
        this.allergenService = allergenService;
        this.passwordGenerator = passwordGenerator;
        this.messageSource = messageSource;
    }

    private String m(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = jwtService.extractRole(request);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("error.auth.adminRequired");
        }
    }

    private User requireActiveUser(Long id) {
        return userRepository.findByIdAndRemovedAtIsNull(id)
                .orElseThrow(() -> new UnauthorizedException("error.auth.userNotFound"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(HttpServletRequest request) {
        checkAdmin(request);
        List<UserProfileResponse> responses = userRepository.findAllByRemovedAtIsNullOrderByIdAsc().stream()
                .map(u -> userProfileAssembler.toResponse(u, false))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id, HttpServletRequest request, Locale locale) {
        checkAdmin(request);
        User user = requireActiveUser(id);
        user.setRemovedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(m("message.admin.userRemoved", locale)));
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetUserPassword(
            @PathVariable Long id,
            HttpServletRequest request,
            Locale locale) {
        checkAdmin(request);
        User user = requireActiveUser(id);
        String plain = passwordGenerator.generateStrong(12);
        user.setPassword(passwordEncoder.encode(plain));
        userRepository.save(user);
        return ResponseEntity.ok(new ResetPasswordResponse(m("message.admin.passwordReset", locale), plain));
    }

    @PutMapping("/users/{id}/blocked")
    public ResponseEntity<MessageResponse> setUserBlocked(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody UserBlockRequest body,
            Locale locale) {
        checkAdmin(request);
        User user = requireActiveUser(id);
        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("error.auth.cannotBlockAdmin");
        }
        if (body.isBlocked()) {
            user.setBlockedAt(LocalDateTime.now());
        } else {
            user.setBlockedAt(null);
        }
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(body.isBlocked()
                ? m("message.admin.userBlocked", locale)
                : m("message.admin.userUnblocked", locale)));
    }

    @GetMapping("/users/{id}/history")
    public ResponseEntity<List<ScanHistoryResponse>> getUserHistory(
            @PathVariable Long id,
            HttpServletRequest request) {
        checkAdmin(request);
        requireActiveUser(id);
        return ResponseEntity.ok(allergenService.getHistoryForUser(id));
    }

    @DeleteMapping("/users/{id}/history")
    public ResponseEntity<MessageResponse> clearUserHistory(
            @PathVariable Long id,
            HttpServletRequest request,
            Locale locale) {
        checkAdmin(request);
        requireActiveUser(id);
        allergenService.clearHistory(id);
        return ResponseEntity.ok(new MessageResponse(m("message.admin.userHistoryCleared", locale)));
    }

    @DeleteMapping("/users/{userId}/history/{historyId}")
    public ResponseEntity<MessageResponse> deleteUserHistoryEntry(
            @PathVariable Long userId,
            @PathVariable Long historyId,
            HttpServletRequest request,
            Locale locale) {
        checkAdmin(request);
        requireActiveUser(userId);
        allergenService.softDeleteHistoryEntry(userId, historyId);
        return ResponseEntity.ok(new MessageResponse(m("message.history.entryRemoved", locale)));
    }

    @PutMapping("/users/{id}/allergens")
    public ResponseEntity<MessageResponse> updateUserAllergens(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody UpdateAllergensRequest payload,
            Locale locale) {
        checkAdmin(request);
        User user = requireActiveUser(id);
        String myAllergens = payload.getMyAllergens();
        user.setMyAllergens(myAllergens == null ? "" : myAllergens.trim());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(m("message.auth.allergensUpdated", locale)));
    }

    @PutMapping("/users/{id}/name")
    public ResponseEntity<UpdateNameResponse> updateUserName(
            @PathVariable Long id,
            HttpServletRequest request,
            @Valid @RequestBody UpdateNameRequest payload,
            Locale locale) {
        checkAdmin(request);
        User user = requireActiveUser(id);
        String newName = payload.getFullName().trim();
        user.setFullName(newName);
        userRepository.save(user);
        UpdateNameResponse response = new UpdateNameResponse();
        response.setMessage(m("message.auth.nameUpdated", locale));
        response.setFullName(user.getFullName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<MessageResponse> updateUserRole(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody RoleUpdateRequest payload,
            Locale locale) {
        checkAdmin(request);
        User user = requireActiveUser(id);
        try {
            Role role = Role.valueOf(payload.getRole().toUpperCase());
            user.setRole(role);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse(m("message.admin.roleUpdated", locale)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(m("error.role.invalid", locale)));
        }
    }

    public static class RoleUpdateRequest {
        private String role;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}

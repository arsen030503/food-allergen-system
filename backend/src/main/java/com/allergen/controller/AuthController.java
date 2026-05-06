package com.allergen.controller;

import com.allergen.dto.auth.AuthLoginResponse;
import com.allergen.dto.auth.AuthRegisterResponse;
import com.allergen.dto.auth.ChangePasswordRequest;
import com.allergen.dto.auth.LoginRequest;
import com.allergen.dto.auth.RegisterRequest;
import com.allergen.dto.auth.UpdateAllergensRequest;
import com.allergen.dto.auth.UpdateAvatarRequest;
import com.allergen.dto.auth.UpdateNameRequest;
import com.allergen.dto.auth.UpdateNameResponse;
import com.allergen.dto.auth.UserProfileAssembler;
import com.allergen.dto.auth.UserProfileResponse;
import com.allergen.dto.common.ApiErrorResponse;
import com.allergen.dto.common.MessageResponse;
import com.allergen.exception.UnauthorizedException;
import com.allergen.model.Role;
import com.allergen.model.User;
import com.allergen.repository.UserRepository;
import com.allergen.security.JwtService;
import com.allergen.service.AvatarImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AvatarImageService avatarImageService;
    private final UserProfileAssembler userProfileAssembler;
    private final MessageSource messageSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthController(UserRepository userRepository,
                          JwtService jwtService,
                          AvatarImageService avatarImageService,
                          UserProfileAssembler userProfileAssembler,
                          MessageSource messageSource) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.avatarImageService = avatarImageService;
        this.userProfileAssembler = userProfileAssembler;
        this.messageSource = messageSource;
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(message));
    }

    private ResponseEntity<ApiErrorResponse> statusError(int status, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(message));
    }

    private String m(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    private String resolveKeyOrText(String raw, Locale locale) {
        if (raw != null && (raw.startsWith("error.") || raw.startsWith("message."))) {
            return m(raw, locale);
        }
        return raw;
    }

    private User getCurrentUser(HttpServletRequest request) {
        Long userId = jwtService.extractUserId(request);
        return userRepository.findByIdAndRemovedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("error.auth.userNotFoundForToken"));
    }

    private User getAuthorizedPathUser(HttpServletRequest request, Long id) {
        User user = getCurrentUser(request);
        if (!user.getId().equals(id)) {
            throw new UnauthorizedException("error.auth.tokenUserMismatch");
        }
        return user;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, Locale locale) {
        String fullName = request.getFullName().trim();
        String email = request.getEmail();
        String password = request.getPassword();
        String normalizedEmail = email == null ? "" : email.toLowerCase(Locale.ROOT).trim();

        if (userRepository.existsByEmailAndRemovedAtIsNull(normalizedEmail)) {
            return badRequest(m("error.auth.emailRegistered", locale));
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setMyAllergens("");
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);
        userRepository.save(user);

        AuthRegisterResponse response = new AuthRegisterResponse();
        response.setMessage(m("message.auth.registrationSuccess", locale));
        String token = jwtService.generateToken(user);
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return ResponseEntity.ok()
                .header("Set-Cookie", jwtService.buildAuthCookie(token).toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, Locale locale) {
        String email = request.getEmail();
        String password = request.getPassword();
        String normalizedEmail = email == null ? "" : email.toLowerCase(Locale.ROOT).trim();

        Optional<User> userOpt = userRepository.findByEmailAndRemovedAtIsNull(normalizedEmail);
        if (userOpt.isEmpty()) {
            return statusError(401, m("error.auth.invalidCredentials", locale));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return statusError(401, m("error.auth.invalidCredentials", locale));
        }
        if (user.getBlockedAt() != null) {
            return statusError(403, m("error.auth.accountBlocked", locale));
        }

        AuthLoginResponse response = new AuthLoginResponse();
        response.setMessage(m("message.auth.loginSuccess", locale));
        String token = jwtService.generateToken(user);
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setMyAllergens(user.getMyAllergens());
        response.setRole(user.getRole().name());
        return ResponseEntity.ok()
                .header("Set-Cookie", jwtService.buildAuthCookie(token).toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(Locale locale) {
        return ResponseEntity.ok()
                .header("Set-Cookie", jwtService.clearAuthCookie().toString())
                .body(new MessageResponse(m("message.auth.loggedOut", locale)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ChangePasswordRequest body,
            Locale locale
    ) {
        User user = getCurrentUser(httpRequest);
        if (!passwordEncoder.matches(body.getCurrentPassword(), user.getPassword())) {
            return badRequest(m("error.auth.currentPasswordIncorrect", locale));
        }
        user.setPassword(passwordEncoder.encode(body.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(m("message.auth.passwordUpdated", locale)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return ResponseEntity.ok(userProfileAssembler.toResponse(user, true));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserProfileResponse> getUser(
            @PathVariable Long id,
            HttpServletRequest request) {
        User user = getAuthorizedPathUser(request, id);
        return ResponseEntity.ok(userProfileAssembler.toResponse(user, true));
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<?> updateAvatar(
            HttpServletRequest httpRequest,
            @RequestBody UpdateAvatarRequest payload,
            Locale locale) {
        User user = getCurrentUser(httpRequest);
        String avatarData = payload.getAvatarData();

        try {
            if (avatarData == null || avatarData.isBlank()) {
                user.setAvatarFull(null);
                user.setAvatarThumb(null);
            } else {
                AvatarImageService.ProcessedAvatar processed = avatarImageService.processFromClientPayload(avatarData);
                user.setAvatarFull(processed.fullJpeg());
                user.setAvatarThumb(processed.thumbJpeg());
            }
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse(m("message.auth.avatarUpdated", locale)));
        } catch (IllegalArgumentException e) {
            return badRequest(resolveKeyOrText(e.getMessage(), locale));
        }
    }

    @PutMapping("/me/allergens")
    public ResponseEntity<MessageResponse> updateCurrentAllergens(
            HttpServletRequest request,
            @RequestBody UpdateAllergensRequest payload,
            Locale locale) {
        User user = getCurrentUser(request);
        String myAllergens = payload.getMyAllergens();
        user.setMyAllergens(myAllergens == null ? "" : myAllergens.trim());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(m("message.auth.allergensUpdated", locale)));
    }

    @PutMapping("/user/{id}/allergens")
    public ResponseEntity<MessageResponse> updateAllergens(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody UpdateAllergensRequest payload,
            Locale locale) {
        User user = getAuthorizedPathUser(request, id);
        String myAllergens = payload.getMyAllergens();
        user.setMyAllergens(myAllergens == null ? "" : myAllergens.trim());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(m("message.auth.allergensUpdated", locale)));
    }

    @PutMapping("/me/name")
    public ResponseEntity<UpdateNameResponse> updateCurrentName(
            HttpServletRequest request,
            @Valid @RequestBody UpdateNameRequest payload,
            Locale locale) {
        User user = getCurrentUser(request);
        String newName = payload.getFullName().trim();
        user.setFullName(newName);
        userRepository.save(user);
        UpdateNameResponse response = new UpdateNameResponse();
        response.setMessage(m("message.auth.nameUpdated", locale));
        response.setFullName(user.getFullName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{id}/name")
    public ResponseEntity<UpdateNameResponse> updateName(
            @PathVariable Long id,
            HttpServletRequest request,
            @Valid @RequestBody UpdateNameRequest payload,
            Locale locale) {
        User user = getAuthorizedPathUser(request, id);
        String newName = payload.getFullName().trim();
        user.setFullName(newName);
        userRepository.save(user);
        UpdateNameResponse response = new UpdateNameResponse();
        response.setMessage(m("message.auth.nameUpdated", locale));
        response.setFullName(user.getFullName());
        return ResponseEntity.ok(response);
    }
}

package com.allergen.controller;

import com.allergen.dto.auth.AuthLoginResponse;
import com.allergen.dto.auth.AuthRegisterResponse;
import com.allergen.dto.auth.LoginRequest;
import com.allergen.dto.auth.RegisterRequest;
import com.allergen.dto.auth.UpdateAllergensRequest;
import com.allergen.dto.auth.UpdateAvatarRequest;
import com.allergen.dto.auth.UpdateNameRequest;
import com.allergen.dto.auth.UpdateNameResponse;
import com.allergen.dto.auth.UserProfileResponse;
import com.allergen.dto.common.ApiErrorResponse;
import com.allergen.dto.common.MessageResponse;
import com.allergen.exception.UnauthorizedException;
import com.allergen.model.Role;
import com.allergen.model.User;
import com.allergen.repository.UserRepository;
import com.allergen.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final int MAX_AVATAR_DATA_LENGTH = 8_000_000;

    @Autowired
    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(message));
    }

    private ResponseEntity<ApiErrorResponse> statusError(int status, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(message));
    }

    private User getCurrentUser(HttpServletRequest request) {
        Long userId = jwtService.extractUserId(request);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found for token"));
    }

    private User getAuthorizedPathUser(HttpServletRequest request, Long id) {
        User user = getCurrentUser(request);
        if (!user.getId().equals(id)) {
            throw new UnauthorizedException("Token user does not match requested user");
        }
        return user;
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setMyAllergens(user.getMyAllergens());
        response.setCreatedAt(user.getCreatedAt());
        response.setAvatarData(user.getAvatarData());
        response.setRole(user.getRole().name());
        return response;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String fullName = request.getFullName().trim();
        String email = request.getEmail();
        String password = request.getPassword();
        String normalizedEmail = email == null ? "" : email.toLowerCase(Locale.ROOT).trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            return badRequest("Email already registered");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setMyAllergens("");
        user.setCreatedAt(LocalDateTime.now().toString());
        // If this is the first user, make them admin
        if (userRepository.count() == 0) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }
        userRepository.save(user);

        AuthRegisterResponse response = new AuthRegisterResponse();
        response.setMessage("Registration successful");
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String normalizedEmail = email == null ? "" : email.toLowerCase(Locale.ROOT).trim();

        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            return statusError(401, "Invalid email or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return statusError(401, "Invalid email or password");
        }

        AuthLoginResponse response = new AuthLoginResponse();
        response.setMessage("Login successful");
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
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok()
                .header("Set-Cookie", jwtService.clearAuthCookie().toString())
                .body(new MessageResponse("Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return ResponseEntity.ok(toUserProfileResponse(user));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserProfileResponse> getUser(
            @PathVariable Long id,
            HttpServletRequest request) {
        User user = getAuthorizedPathUser(request, id);
        return ResponseEntity.ok(toUserProfileResponse(user));
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<?> updateAvatar(
            HttpServletRequest httpRequest,
            @RequestBody UpdateAvatarRequest payload) {
        User user = getCurrentUser(httpRequest);
        String avatarData = payload.getAvatarData();

        if (avatarData != null && avatarData.length() > MAX_AVATAR_DATA_LENGTH) {
            return badRequest("Avatar is too large");
        }

        user.setAvatarData(avatarData == null || avatarData.isBlank() ? null : avatarData.trim());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Avatar updated"));
    }

    @PutMapping("/me/allergens")
    public ResponseEntity<MessageResponse> updateCurrentAllergens(
            HttpServletRequest request,
            @RequestBody UpdateAllergensRequest payload) {
        User user = getCurrentUser(request);
        String myAllergens = payload.getMyAllergens();
        user.setMyAllergens(myAllergens == null ? "" : myAllergens.trim());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Allergens updated"));
    }

    @PutMapping("/user/{id}/allergens")
    public ResponseEntity<MessageResponse> updateAllergens(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody UpdateAllergensRequest payload) {
        User user = getAuthorizedPathUser(request, id);
        String myAllergens = payload.getMyAllergens();
        user.setMyAllergens(myAllergens == null ? "" : myAllergens.trim());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Allergens updated"));
    }

    @PutMapping("/me/name")
    public ResponseEntity<UpdateNameResponse> updateCurrentName(
            HttpServletRequest request,
            @Valid @RequestBody UpdateNameRequest payload) {
        User user = getCurrentUser(request);
        String newName = payload.getFullName().trim();
        user.setFullName(newName);
        userRepository.save(user);
        UpdateNameResponse response = new UpdateNameResponse();
        response.setMessage("Name updated");
        response.setFullName(user.getFullName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{id}/name")
    public ResponseEntity<UpdateNameResponse> updateName(
            @PathVariable Long id,
            HttpServletRequest request,
            @Valid @RequestBody UpdateNameRequest payload) {
        User user = getAuthorizedPathUser(request, id);
        String newName = payload.getFullName().trim();
        user.setFullName(newName);
        userRepository.save(user);
        UpdateNameResponse response = new UpdateNameResponse();
        response.setMessage("Name updated");
        response.setFullName(user.getFullName());
        return ResponseEntity.ok(response);
    }
}

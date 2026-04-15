package com.allergen.controller;

import com.allergen.dto.auth.UpdateAllergensRequest;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public AdminController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    private ResponseEntity<ApiErrorResponse> forbidden(String message) {
        return ResponseEntity.status(403).body(new ApiErrorResponse(message));
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = jwtService.extractRole(request);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("Admin access required");
        }
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

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(HttpServletRequest request) {
        checkAdmin(request);
        List<User> users = userRepository.findAll();
        List<UserProfileResponse> responses = users.stream()
                .map(this::toUserProfileResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("User deleted"));
    }

    @PutMapping("/users/{id}/allergens")
    public ResponseEntity<MessageResponse> updateUserAllergens(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody UpdateAllergensRequest payload) {
        checkAdmin(request);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        String myAllergens = payload.getMyAllergens();
        user.setMyAllergens(myAllergens == null ? "" : myAllergens.trim());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Allergens updated"));
    }

    @PutMapping("/users/{id}/name")
    public ResponseEntity<UpdateNameResponse> updateUserName(
            @PathVariable Long id,
            HttpServletRequest request,
            @Valid @RequestBody UpdateNameRequest payload) {
        checkAdmin(request);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        String newName = payload.getFullName().trim();
        user.setFullName(newName);
        userRepository.save(user);
        UpdateNameResponse response = new UpdateNameResponse();
        response.setMessage("Name updated");
        response.setFullName(user.getFullName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<MessageResponse> updateUserRole(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestBody RoleUpdateRequest payload) {
        checkAdmin(request);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        try {
            Role role = Role.valueOf(payload.getRole().toUpperCase());
            user.setRole(role);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("Role updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid role"));
        }
    }

    public static class RoleUpdateRequest {
        private String role;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}


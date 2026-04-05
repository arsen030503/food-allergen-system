package com.allergen;

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

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String fullName = request.get("fullName");
        String email = request.get("email");
        String password = request.get("password");

        if (fullName == null || fullName.trim().isEmpty()) {
            response.put("error", "Full name is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (email == null || !email.contains("@")) {
            response.put("error", "Valid email is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (password == null || password.length() < 6) {
            response.put("error", "Password must be at least 6 characters");
            return ResponseEntity.badRequest().body(response);
        }
        if (userRepository.existsByEmail(email)) {
            response.put("error", "Email already registered");
            return ResponseEntity.badRequest().body(response);
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setMyAllergens("");
        user.setCreatedAt(LocalDateTime.now().toString());
        userRepository.save(user);

        response.put("message", "Registration successful");
        response.put("userId", user.getId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            response.put("error", "Email and password are required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOpt.isEmpty()) {
            response.put("error", "Invalid email or password");
            return ResponseEntity.status(401).body(response);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("error", "Invalid email or password");
            return ResponseEntity.status(401).body(response);
        }

        response.put("message", "Login successful");
        response.put("userId", user.getId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("myAllergens", user.getMyAllergens());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            response.put("error", "User not found");
            return ResponseEntity.status(404).body(response);
        }
        User user = userOpt.get();
        response.put("userId", user.getId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("myAllergens", user.getMyAllergens());
        response.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{id}/allergens")
    public ResponseEntity<Map<String, Object>> updateAllergens(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            response.put("error", "User not found");
            return ResponseEntity.status(404).body(response);
        }
        User user = userOpt.get();
        user.setMyAllergens(request.get("myAllergens"));
        userRepository.save(user);
        response.put("message", "Allergens updated");
        return ResponseEntity.ok(response);
    }
}
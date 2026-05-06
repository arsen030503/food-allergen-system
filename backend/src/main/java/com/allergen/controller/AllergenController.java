package com.allergen.controller;

import com.allergen.dto.allergen.AnalyzeRequest;
import com.allergen.dto.allergen.AnalyzeResponse;
import com.allergen.dto.allergen.HealthResponse;
import com.allergen.dto.allergen.ScanHistoryResponse;
import com.allergen.dto.allergen.ScanStatsResponse;
import com.allergen.dto.common.MessageResponse;
import com.allergen.model.Allergen;
import com.allergen.security.JwtService;
import com.allergen.service.AllergenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AllergenController {

    private final AllergenService allergenService;
    private final JwtService jwtService;

    @Autowired
    public AllergenController(AllergenService allergenService, JwtService jwtService) {
        this.allergenService = allergenService;
        this.jwtService = jwtService;
    }

    // POST /api/analyze - analyze ingredients
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            HttpServletRequest httpRequest) {
        String ingredients = request.getIngredients().trim();
        Long userId = jwtService.extractUserId(httpRequest);
        String productName = request.getProductName();
        Map<String, Object> result = allergenService.analyzeIngredients(ingredients, userId, productName);

        AnalyzeResponse response = new AnalyzeResponse();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> detected = (List<Map<String, String>>) result.getOrDefault("detectedAllergens", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> matchesForUser = (List<Map<String, String>>) result.getOrDefault("matchesForUser", new ArrayList<>());
        response.setDetectedAllergens(detected);
        response.setMatchesForUser(matchesForUser);
        response.setTotalFound((int) result.getOrDefault("totalFound", detected.size()));
        response.setSafe((boolean) result.getOrDefault("safe", detected.isEmpty()));
        response.setUserProfileConfigured((boolean) result.getOrDefault("userProfileConfigured", false));
        response.setSafeForUser((boolean) result.getOrDefault("safeForUser", false));
        return ResponseEntity.ok(response);
    }

    // GET /api/allergens - get all allergens in database
    @GetMapping("/allergens")
    public ResponseEntity<List<Allergen>> getAllAllergens() {
        return ResponseEntity.ok(allergenService.getAllAllergens());
    }

    // GET /api/history - get last 10 scans for current user
    @GetMapping("/history")
    public ResponseEntity<List<ScanHistoryResponse>> getHistory(
            HttpServletRequest httpRequest) {
        Long userId = jwtService.extractUserId(httpRequest);
        return ResponseEntity.ok(allergenService.getHistoryForUser(userId));
    }

    @GetMapping("/stats")
    public ResponseEntity<ScanStatsResponse> getStats(HttpServletRequest httpRequest) {
        Long userId = jwtService.extractUserId(httpRequest);
        return ResponseEntity.ok(allergenService.getScanStats(userId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<MessageResponse> clearHistory(
            HttpServletRequest httpRequest) {
        Long userId = jwtService.extractUserId(httpRequest);
        allergenService.clearHistory(userId);
        return ResponseEntity.ok(new MessageResponse("History cleared"));
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<MessageResponse> deleteHistoryEntry(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = jwtService.extractUserId(httpRequest);
        allergenService.softDeleteHistoryEntry(userId, id);
        return ResponseEntity.ok(new MessageResponse("History entry removed"));
    }

    // GET /api/health - check if server is running
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse();
        response.setStatus("running");
        response.setApp("Food Allergen Detection System");
        return ResponseEntity.ok(response);
    }
}

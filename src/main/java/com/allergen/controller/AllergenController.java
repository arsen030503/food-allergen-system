package com.allergen.controller;

import com.allergen.dto.allergen.AnalyzeRequest;
import com.allergen.dto.allergen.AnalyzeResponse;
import com.allergen.dto.allergen.HealthResponse;
import com.allergen.dto.common.MessageResponse;
import com.allergen.model.Allergen;
import com.allergen.model.ScanHistory;
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
@CrossOrigin(origins = "*")
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
        response.setDetectedAllergens(detected);
        response.setTotalFound((int) result.getOrDefault("totalFound", detected.size()));
        response.setSafe((boolean) result.getOrDefault("safe", detected.isEmpty()));
        return ResponseEntity.ok(response);
    }

    // GET /api/allergens - get all allergens in database
    @GetMapping("/allergens")
    public ResponseEntity<List<Allergen>> getAllAllergens() {
        return ResponseEntity.ok(allergenService.getAllAllergens());
    }

    // GET /api/history - get last 10 scans for current user
    @GetMapping("/history")
    public ResponseEntity<List<ScanHistory>> getHistory(
            HttpServletRequest httpRequest) {
        Long userId = jwtService.extractUserId(httpRequest);
        return ResponseEntity.ok(allergenService.getHistory(userId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<MessageResponse> clearHistory(
            HttpServletRequest httpRequest) {
        Long userId = jwtService.extractUserId(httpRequest);
        allergenService.clearHistory(userId);
        return ResponseEntity.ok(new MessageResponse("History cleared"));
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

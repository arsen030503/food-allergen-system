package com.allergen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AllergenController {

    @Autowired
    private AllergenService allergenService;

    // POST /api/analyze - analyze ingredients
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody Map<String, String> request) {
        String ingredients = request.get("ingredients");
        if (ingredients == null || ingredients.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No ingredients provided");
            return ResponseEntity.badRequest().body(error);
        }
        Map<String, Object> result = allergenService.analyzeIngredients(ingredients);
        return ResponseEntity.ok(result);
    }

    // GET /api/allergens - get all allergens in database
    @GetMapping("/allergens")
    public ResponseEntity<List<Allergen>> getAllAllergens() {
        return ResponseEntity.ok(allergenService.getAllAllergens());
    }

    // GET /api/history - get last 10 scans
    @GetMapping("/history")
    public ResponseEntity<List<ScanHistory>> getHistory() {
        return ResponseEntity.ok(allergenService.getHistory());
    }

    // GET /api/health - check if server is running
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "running");
        status.put("app", "Food Allergen Detection System");
        return ResponseEntity.ok(status);
    }
}
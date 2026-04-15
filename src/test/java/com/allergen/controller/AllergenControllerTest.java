package com.allergen.controller;

import com.allergen.dto.allergen.AnalyzeRequest;
import com.allergen.dto.allergen.AnalyzeResponse;
import com.allergen.dto.allergen.HealthResponse;
import com.allergen.dto.common.MessageResponse;
import com.allergen.security.JwtService;
import com.allergen.service.AllergenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllergenControllerTest {

    @Mock
    private AllergenService allergenService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AllergenController allergenController;

    @Test
    void analyzeMapsServicePayloadToTypedResponse() {
        HttpServletRequest servletRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        AnalyzeRequest request = new AnalyzeRequest();
        request.setIngredients("milk, sugar");
        request.setProductName("Bar");

        Map<String, String> detected = Map.of(
                "allergen", "Milk",
                "severity", "HIGH",
                "matchedIngredient", "milk"
        );
        when(allergenService.analyzeIngredients("milk, sugar", 3L, "Bar")).thenReturn(Map.of(
                "detectedAllergens", List.of(detected),
                "totalFound", 1,
                "safe", false
        ));
        when(jwtService.extractUserId(servletRequest)).thenReturn(3L);

        ResponseEntity<AnalyzeResponse> response = allergenController.analyze(request, servletRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalFound());
        assertFalse(response.getBody().isSafe());
        assertEquals("Milk", response.getBody().getDetectedAllergens().getFirst().get("allergen"));
        verify(allergenService).analyzeIngredients("milk, sugar", 3L, "Bar");
        verify(jwtService).extractUserId(servletRequest);
    }

    @Test
    void healthReturnsTypedPayload() {
        ResponseEntity<HealthResponse> response = allergenController.health();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("running", response.getBody().getStatus());
        assertEquals("Food Allergen Detection System", response.getBody().getApp());
    }

    @Test
    void clearHistoryUsesTokenUserId() {
        HttpServletRequest servletRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(jwtService.extractUserId(servletRequest)).thenReturn(3L);

        ResponseEntity<MessageResponse> response = allergenController.clearHistory(servletRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("History cleared", response.getBody().getMessage());
        verify(allergenService).clearHistory(3L);
    }
}




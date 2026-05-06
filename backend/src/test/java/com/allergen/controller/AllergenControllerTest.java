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
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;
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

    private AllergenController allergenController;

    private static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        allergenController = new AllergenController(allergenService, jwtService, messageSource());
    }

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
                "matchesForUser", List.of(detected),
                "totalFound", 1,
                "safe", false,
                "userProfileConfigured", true,
                "safeForUser", false
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
        ResponseEntity<HealthResponse> response = allergenController.health(Locale.ENGLISH);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("running", response.getBody().getStatus());
        assertEquals("Food Allergen Detection System", response.getBody().getApp());
    }

    @Test
    void clearHistoryUsesTokenUserId() {
        HttpServletRequest servletRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(jwtService.extractUserId(servletRequest)).thenReturn(3L);

        ResponseEntity<MessageResponse> response = allergenController.clearHistory(servletRequest, Locale.ENGLISH);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("History cleared", response.getBody().getMessage());
        verify(allergenService).clearHistory(3L);
    }

    @Test
    void deleteHistoryEntryUsesTokenUserId() {
        HttpServletRequest servletRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(jwtService.extractUserId(servletRequest)).thenReturn(3L);

        ResponseEntity<MessageResponse> response = allergenController.deleteHistoryEntry(9L, servletRequest, Locale.ENGLISH);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("History entry removed", response.getBody().getMessage());
        verify(allergenService).softDeleteHistoryEntry(3L, 9L);
    }
}




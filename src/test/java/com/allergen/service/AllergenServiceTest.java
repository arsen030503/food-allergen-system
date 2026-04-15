package com.allergen.service;

import com.allergen.model.Allergen;
import com.allergen.model.ScanHistory;
import com.allergen.repository.AllergenRepository;
import com.allergen.repository.ScanHistoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllergenServiceTest {

    @Mock
    private AllergenRepository allergenRepository;

    @Mock
    private ScanHistoryRepository scanHistoryRepository;

    @InjectMocks
    private AllergenService allergenService;

    @Test
    void analyzeIngredientsDetectsConfiguredAllergen() {
        Allergen milk = new Allergen();
        milk.setName("Milk");
        milk.setStandard("EU");
        milk.setSeverity("HIGH");
        milk.setDescription("Milk and dairy products");
        milk.setTriggerIngredients("milk,cream,whey");

        when(allergenRepository.findAll()).thenReturn(List.of(milk));

        Map<String, Object> result = allergenService.analyzeIngredients("sugar, whole milk powder", 7L, "Chocolate");

        assertEquals(1, result.get("totalFound"));
        assertEquals(Boolean.FALSE, result.get("safe"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> detected = (List<Map<String, String>>) result.get("detectedAllergens");
        assertEquals(1, detected.size());
        assertEquals("Milk", detected.getFirst().get("allergen"));

        ArgumentCaptor<ScanHistory> captor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository).save(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals("Chocolate", captor.getValue().getProductName());
        assertEquals("Milk", captor.getValue().getDetectedAllergens());
    }

    @Test
    void analyzeIngredientsDoesNotMatchPartialWord() {
        Allergen milk = new Allergen();
        milk.setName("Milk");
        milk.setStandard("EU");
        milk.setSeverity("HIGH");
        milk.setDescription("Milk and dairy products");
        milk.setTriggerIngredients("milk");

        when(allergenRepository.findAll()).thenReturn(List.of(milk));

        Map<String, Object> result = allergenService.analyzeIngredients("milkweed extract, sugar", null, "");

        assertEquals(0, result.get("totalFound"));
        assertEquals(Boolean.TRUE, result.get("safe"));

        ArgumentCaptor<ScanHistory> captor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository).save(captor.capture());
        assertEquals("Unnamed Product", captor.getValue().getProductName());
        assertEquals("", captor.getValue().getDetectedAllergens());
    }

    @Test
    void getHistoryRequiresUserId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> allergenService.getHistory(null));
        assertEquals("userId is required", ex.getMessage());
    }

    @Test
    void getHistoryUsesUserScopedRepositoryCall() {
        when(scanHistoryRepository.findTop10ByUserIdOrderByScannedAtDesc(11L)).thenReturn(List.of());

        List<ScanHistory> result = allergenService.getHistory(11L);

        assertNotNull(result);
        verify(scanHistoryRepository).findTop10ByUserIdOrderByScannedAtDesc(11L);
        verify(scanHistoryRepository, never()).findTop10ByOrderByScannedAtDesc();
    }

    @Test
    void clearHistoryUsesUserScopedRepositoryDelete() {
        allergenService.clearHistory(11L);

        verify(scanHistoryRepository).deleteByUserId(11L);
    }
}



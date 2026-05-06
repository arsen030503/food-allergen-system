package com.allergen.service;

import com.allergen.model.Allergen;
import com.allergen.model.ScanHistory;
import com.allergen.model.User;
import com.allergen.repository.AllergenRepository;
import com.allergen.repository.ScanHistoryRepository;
import com.allergen.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllergenServiceTest {

    @Mock
    private AllergenRepository allergenRepository;

    @Mock
    private ScanHistoryRepository scanHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AllergenService allergenService;

    private static User userWithProfile(long id, String profileCsv) {
        User u = new User();
        u.setId(id);
        u.setMyAllergens(profileCsv);
        return u;
    }

    @Test
    void analyzeIngredientsDetectsConfiguredAllergen() {
        Allergen milk = new Allergen();
        milk.setName("Milk");
        milk.setStandard("EU");
        milk.setSeverity("HIGH");
        milk.setDescription("Milk and dairy products");
        milk.setTriggerIngredients("milk,cream,whey");

        when(userRepository.findByIdAndRemovedAtIsNull(7L)).thenReturn(Optional.of(userWithProfile(7L, "Milk,Soy")));

        when(allergenRepository.findAll()).thenReturn(List.of(milk));

        Map<String, Object> result = allergenService.analyzeIngredients("sugar, whole milk powder", 7L, "Chocolate");

        assertEquals(1, result.get("totalFound"));
        assertEquals(Boolean.FALSE, result.get("safe"));
        assertEquals(Boolean.TRUE, result.get("userProfileConfigured"));
        assertEquals(Boolean.FALSE, result.get("safeForUser"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> detected = (List<Map<String, String>>) result.get("detectedAllergens");
        assertEquals(1, detected.size());
        assertEquals("Milk", detected.getFirst().get("allergen"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> forUser = (List<Map<String, String>>) result.get("matchesForUser");
        assertEquals(1, forUser.size());
        assertEquals("Milk", forUser.getFirst().get("allergen"));

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

        when(userRepository.findByIdAndRemovedAtIsNull(99L)).thenReturn(Optional.of(userWithProfile(99L, "")));

        when(allergenRepository.findAll()).thenReturn(List.of(milk));

        Map<String, Object> result = allergenService.analyzeIngredients("milkweed extract, sugar", 99L, "");

        assertEquals(0, result.get("totalFound"));
        assertEquals(Boolean.TRUE, result.get("safe"));
        assertEquals(Boolean.FALSE, result.get("userProfileConfigured"));
        assertEquals(Boolean.FALSE, result.get("safeForUser"));

        ArgumentCaptor<ScanHistory> captor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository).save(captor.capture());
        assertEquals("Unnamed Product", captor.getValue().getProductName());
        assertEquals("", captor.getValue().getDetectedAllergens());
    }

    @Test
    void analyzeIngredientsSafeForUserWhenProfileDoesNotOverlap() {
        Allergen milk = new Allergen();
        milk.setName("Milk");
        milk.setStandard("EU");
        milk.setSeverity("HIGH");
        milk.setDescription("x");
        milk.setTriggerIngredients("milk");

        when(userRepository.findByIdAndRemovedAtIsNull(3L)).thenReturn(Optional.of(userWithProfile(3L, "Peanuts")));

        when(allergenRepository.findAll()).thenReturn(List.of(milk));

        Map<String, Object> result = allergenService.analyzeIngredients("sugar, milk", 3L, "X");

        assertEquals(1, result.get("totalFound"));
        assertEquals(Boolean.TRUE, result.get("userProfileConfigured"));
        assertEquals(Boolean.TRUE, result.get("safeForUser"));

        @SuppressWarnings("unchecked")
        List<Map<String, String>> forUser = (List<Map<String, String>>) result.get("matchesForUser");
        assertTrue(forUser.isEmpty());
    }

    @Test
    void getHistoryRequiresUserId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> allergenService.getHistoryForUser(null));
        assertEquals("error.userId.required", ex.getMessage());
    }

    @Test
    void getHistoryUsesUserScopedRepositoryCall() {
        when(userRepository.findByIdAndRemovedAtIsNull(11L)).thenReturn(Optional.of(userWithProfile(11L, "Milk")));
        when(scanHistoryRepository.findTop10ByUserIdAndRemovedAtIsNullOrderByScannedAtDesc(11L)).thenReturn(List.of());

        assertNotNull(allergenService.getHistoryForUser(11L));
        verify(scanHistoryRepository).findTop10ByUserIdAndRemovedAtIsNullOrderByScannedAtDesc(11L);
    }

    @Test
    void clearHistorySoftDeletesRowsForUser() {
        allergenService.clearHistory(11L);

        verify(scanHistoryRepository).softDeleteAllForUser(eq(11L), any(LocalDateTime.class));
    }
}

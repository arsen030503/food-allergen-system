package com.allergen.service;

import com.allergen.dto.allergen.ScanHistoryResponse;
import com.allergen.dto.allergen.ScanStatsResponse;
import com.allergen.exception.ForbiddenException;
import com.allergen.model.Allergen;
import com.allergen.model.ScanHistory;
import com.allergen.model.User;
import com.allergen.repository.AllergenRepository;
import com.allergen.repository.ScanHistoryRepository;
import com.allergen.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.*;

@Service
public class AllergenService {

    private final AllergenRepository allergenRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public AllergenService(AllergenRepository allergenRepository,
                           ScanHistoryRepository scanHistoryRepository,
                           UserRepository userRepository) {
        this.allergenRepository = allergenRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.userRepository = userRepository;
    }

    private static Set<String> parseProfileCsv(String csv) {
        Set<String> set = new HashSet<>();
        if (csv == null || csv.isBlank()) {
            return set;
        }
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }

    private static Set<String> detectedNamesFromHistoryField(String detectedAllergensCsv) {
        Set<String> names = new HashSet<>();
        if (detectedAllergensCsv == null || detectedAllergensCsv.isBlank()) {
            return names;
        }
        for (String part : detectedAllergensCsv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                names.add(trimmed);
            }
        }
        return names;
    }

    private static int countProfileIntersection(Set<String> detectedNames, Set<String> profile) {
        if (profile.isEmpty()) {
            return 0;
        }
        int c = 0;
        for (String name : detectedNames) {
            if (profile.contains(name)) {
                c++;
            }
        }
        return c;
    }

    private ScanHistoryResponse toHistoryResponse(ScanHistory row, Set<String> profile, boolean profileConfigured) {
        Set<String> detectedNames = detectedNamesFromHistoryField(row.getDetectedAllergens());
        int profileMatchCount = countProfileIntersection(detectedNames, profile);
        ScanHistoryResponse r = new ScanHistoryResponse();
        r.setId(row.getId());
        r.setIngredients(row.getIngredients());
        r.setScannedAt(row.getScannedAt());
        r.setAllergenCount(row.getAllergenCount());
        r.setProductName(row.getProductName());
        r.setDetectedAllergens(row.getDetectedAllergens());
        r.setProfileMatchCount(profileMatchCount);
        r.setUserProfileConfigured(profileConfigured);
        r.setSafeForUser(!profileConfigured || profileMatchCount == 0);
        return r;
    }

    private boolean matchesTrigger(String ingredient, String trigger) {
        if (trigger.isBlank() || ingredient.isBlank()) {
            return false;
        }
        String escapedTrigger = Pattern.quote(trigger.trim().toLowerCase(Locale.ROOT));
        String pattern = "(^|[^a-z0-9])" + escapedTrigger + "([^a-z0-9]|$)";
        return ingredient.toLowerCase(Locale.ROOT).matches(".*" + pattern + ".*");
    }

    public Map<String, Object> analyzeIngredients(String ingredientsText, Long userId, String productName) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        User user = userRepository.findByIdAndRemovedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getBlockedAt() != null) {
            throw new ForbiddenException("Account is blocked");
        }
        Set<String> profile = parseProfileCsv(user.getMyAllergens());
        boolean userProfileConfigured = !profile.isEmpty();

        String[] inputIngredients = ingredientsText.split("[,;\\n]+");
        List<Allergen> allAllergens = allergenRepository.findAll();
        List<Map<String, String>> detected = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Allergen allergen : allAllergens) {
            String trigCsv = allergen.getTriggerIngredients();
            if (trigCsv == null || trigCsv.isBlank()) {
                continue;
            }
            String[] triggers = trigCsv.split(",");
            for (String trigger : triggers) {
                trigger = trigger.trim();
                for (String input : inputIngredients) {
                    input = input.trim();
                    if (!seen.contains(allergen.getName()) && matchesTrigger(input, trigger)) {
                        Map<String, String> result = new HashMap<>();
                        result.put("allergen", allergen.getName());
                        result.put("standard", allergen.getStandard());
                        result.put("severity", allergen.getSeverity());
                        result.put("description", allergen.getDescription());
                        result.put("matchedIngredient", input);
                        detected.add(result);
                        seen.add(allergen.getName());
                        break;
                    }
                }
                if (seen.contains(allergen.getName())) {
                    break;
                }
            }
        }

        List<Map<String, String>> matchesForUser = detected.stream()
                .filter(m -> profile.contains(m.get("allergen")))
                .toList();

        ScanHistory history = new ScanHistory();
        history.setIngredients(ingredientsText);
        history.setDetectedAllergens(String.join(",", seen));
        history.setAllergenCount(detected.size());
        history.setUserId(userId);
        history.setProductName(productName == null || productName.isBlank() ? "Unnamed Product" : productName.trim());
        scanHistoryRepository.save(history);

        Map<String, Object> response = new HashMap<>();
        response.put("detectedAllergens", detected);
        response.put("matchesForUser", new ArrayList<>(matchesForUser));
        response.put("totalFound", detected.size());
        response.put("safe", detected.isEmpty());
        response.put("userProfileConfigured", userProfileConfigured);
        response.put("safeForUser", userProfileConfigured && matchesForUser.isEmpty());
        return response;
    }

    public List<Allergen> getAllAllergens() {
        return allergenRepository.findAll();
    }

    public List<ScanHistoryResponse> getHistoryForUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        User user = userRepository.findByIdAndRemovedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<String> profile = parseProfileCsv(user.getMyAllergens());
        boolean configured = !profile.isEmpty();
        return scanHistoryRepository.findTop10ByUserIdAndRemovedAtIsNullOrderByScannedAtDesc(userId).stream()
                .map(row -> toHistoryResponse(row, profile, configured))
                .toList();
    }

    public ScanStatsResponse getScanStats(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        User user = userRepository.findByIdAndRemovedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<String> profile = parseProfileCsv(user.getMyAllergens());
        boolean configured = !profile.isEmpty();

        long total = scanHistoryRepository.countByUserIdAndRemovedAtIsNull(userId);
        List<ScanHistory> rows = scanHistoryRepository.findByUserIdAndRemovedAtIsNullOrderByScannedAtDesc(userId);
        int safe = 0;
        for (ScanHistory row : rows) {
            Set<String> detectedNames = detectedNamesFromHistoryField(row.getDetectedAllergens());
            int pm = countProfileIntersection(detectedNames, profile);
            if (!configured || pm == 0) {
                safe++;
            }
        }

        ScanStatsResponse stats = new ScanStatsResponse();
        stats.setTotalScans((int) total);
        stats.setSafeScans(safe);
        stats.setUserAllergenCount(profile.size());
        return stats;
    }

    @Transactional
    public void clearHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        scanHistoryRepository.softDeleteAllForUser(userId, LocalDateTime.now());
    }

    @Transactional
    public void softDeleteHistoryEntry(Long userId, Long historyId) {
        if (userId == null || historyId == null) {
            throw new IllegalArgumentException("userId and historyId are required");
        }
        ScanHistory row = scanHistoryRepository.findByIdAndUserIdAndRemovedAtIsNull(historyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("History entry not found"));
        row.setRemovedAt(LocalDateTime.now());
        scanHistoryRepository.save(row);
    }
}

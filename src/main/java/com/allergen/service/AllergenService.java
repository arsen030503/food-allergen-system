package com.allergen.service;

import com.allergen.model.Allergen;
import com.allergen.model.ScanHistory;
import com.allergen.repository.AllergenRepository;
import com.allergen.repository.ScanHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.regex.Pattern;
import java.util.*;

@Service
public class AllergenService {

    private final AllergenRepository allergenRepository;
    private final ScanHistoryRepository scanHistoryRepository;

    @Autowired
    public AllergenService(AllergenRepository allergenRepository,
                           ScanHistoryRepository scanHistoryRepository) {
        this.allergenRepository = allergenRepository;
        this.scanHistoryRepository = scanHistoryRepository;
    }

    // Load allergen database on startup
    @PostConstruct
    public void initAllergenDatabase() {
        if (allergenRepository.count() > 0) return;

        List<Allergen> allergens = new ArrayList<>();

        // EU 14 Major Allergens
        allergens.add(create("Gluten", "EU", "wheat,barley,rye,oats,spelt,kamut,flour,bread,pasta,semolina,wheat starch", "HIGH", "Cereals containing gluten"));
        allergens.add(create("Crustaceans", "EU", "shrimp,crab,lobster,prawn,crayfish,langoustine", "HIGH", "Crustacean shellfish"));
        allergens.add(create("Eggs", "EU", "egg,eggs,albumin,mayonnaise,meringue,lysozyme,ovalbumin", "HIGH", "Eggs and egg products"));
        allergens.add(create("Fish", "EU", "fish,cod,salmon,tuna,mackerel,anchovies,sardines,bass,flounder,haddock", "HIGH", "Fish and fish products"));
        allergens.add(create("Peanuts", "EU", "peanut,peanuts,groundnut,groundnuts,monkey nuts,peanut oil,peanut butter", "HIGH", "Peanuts and peanut products"));
        allergens.add(create("Soybeans", "EU", "soy,soya,soybean,tofu,tempeh,miso,edamame,soy sauce,tamari", "HIGH", "Soybeans and soy products"));
        allergens.add(create("Milk", "EU", "milk,dairy,cheese,butter,cream,yogurt,lactose,whey,casein,ghee", "HIGH", "Milk and dairy products"));
        allergens.add(create("Nuts", "EU", "almond,hazelnut,walnut,cashew,pecan,brazil nut,pistachio,macadamia,pine nut", "HIGH", "Tree nuts"));
        allergens.add(create("Celery", "EU", "celery,celeriac,celery seed,celery salt", "MEDIUM", "Celery and celeriac"));
        allergens.add(create("Mustard", "EU", "mustard,mustard seed,mustard oil,mustard leaves", "MEDIUM", "Mustard and mustard products"));
        allergens.add(create("Sesame", "EU", "sesame,sesame seed,tahini,sesame oil,til", "HIGH", "Sesame seeds and products"));
        allergens.add(create("Sulphites", "EU", "sulphite,sulfite,sulphur dioxide,sulfur dioxide,so2,e220,e221,e222,e223,e224", "MEDIUM", "Sulphur dioxide and sulphites"));
        allergens.add(create("Lupin", "EU", "lupin,lupine,lupin flour,lupin seed", "MEDIUM", "Lupin and lupin products"));
        allergens.add(create("Molluscs", "EU", "mollusc,mollusk,squid,octopus,clam,oyster,scallop,mussel,snail", "HIGH", "Molluscs and mollusc products"));

        // FDA 9 Major Allergens
        allergens.add(create("Wheat", "FDA", "wheat,flour,bread,pasta,semolina,wheat starch,bulgur,farro", "HIGH", "Wheat and wheat products"));
        allergens.add(create("Shellfish", "FDA", "shrimp,crab,lobster,clam,oyster,scallop,mussel,squid", "HIGH", "Shellfish"));
        allergens.add(create("Tree Nuts", "FDA", "almond,cashew,walnut,pecan,pistachio,brazil nut,hazelnut,macadamia", "HIGH", "Tree nuts"));
        allergens.add(create("Sesame Seeds", "FDA", "sesame,tahini,sesame oil,sesame seed,til,gingelly", "HIGH", "Sesame seeds"));

        allergenRepository.saveAll(allergens);
    }

    private Allergen create(String name, String standard, String triggers, String severity, String description) {
        Allergen a = new Allergen();
        a.setName(name);
        a.setStandard(standard);
        a.setTriggerIngredients(triggers);
        a.setSeverity(severity);
        a.setDescription(description);
        return a;
    }

    private boolean matchesTrigger(String ingredient, String trigger) {
        if (trigger.isBlank() || ingredient.isBlank()) {
            return false;
        }
        String escapedTrigger = Pattern.quote(trigger.trim().toLowerCase(Locale.ROOT));
        String pattern = "(^|[^a-z0-9])" + escapedTrigger + "([^a-z0-9]|$)";
        return ingredient.toLowerCase(Locale.ROOT).matches(".*" + pattern + ".*");
    }

    // Main detection method
    public Map<String, Object> analyzeIngredients(String ingredientsText, Long userId, String productName) {
        String[] inputIngredients = ingredientsText.split("[,;\\n]+");
        List<Allergen> allAllergens = allergenRepository.findAll();
        List<Map<String, String>> detected = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Allergen allergen : allAllergens) {
            String[] triggers = allergen.getTriggerIngredients().split(",");
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

        // Save to history
        ScanHistory history = new ScanHistory();
        history.setIngredients(ingredientsText);
        history.setDetectedAllergens(String.join(",", seen));
        history.setAllergenCount(detected.size());
        history.setUserId(userId);
        history.setProductName(productName == null || productName.isBlank() ? "Unnamed Product" : productName.trim());
        scanHistoryRepository.save(history);

        Map<String, Object> response = new HashMap<>();
        response.put("detectedAllergens", detected);
        response.put("totalFound", detected.size());
        response.put("safe", detected.isEmpty());
        return response;
    }

    public List<Allergen> getAllAllergens() {
        return allergenRepository.findAll();
    }

    public List<ScanHistory> getHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return scanHistoryRepository.findTop10ByUserIdOrderByScannedAtDesc(userId);
    }

    public void clearHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        scanHistoryRepository.deleteByUserId(userId);
    }
}


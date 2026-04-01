package com.allergen;

import jakarta.persistence.*;

@Entity
@Table(name = "allergens")
public class Allergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String standard; // EU or FDA

    @Column(columnDefinition = "TEXT")
    private String triggerIngredients; // comma-separated

    @Column
    private String severity; // HIGH, MEDIUM, LOW

    @Column
    private String description;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getTriggerIngredients() { return triggerIngredients; }
    public void setTriggerIngredients(String triggerIngredients) { this.triggerIngredients = triggerIngredients; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
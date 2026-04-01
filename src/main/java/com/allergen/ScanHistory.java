package com.allergen;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_history")
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String ingredients;

    @Column(columnDefinition = "TEXT")
    private String detectedAllergens;

    @Column
    private LocalDateTime scannedAt;

    @Column
    private int allergenCount;

    @PrePersist
    public void prePersist() {
        scannedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getDetectedAllergens() { return detectedAllergens; }
    public void setDetectedAllergens(String detectedAllergens) { this.detectedAllergens = detectedAllergens; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public int getAllergenCount() { return allergenCount; }
    public void setAllergenCount(int allergenCount) { this.allergenCount = allergenCount; }
}
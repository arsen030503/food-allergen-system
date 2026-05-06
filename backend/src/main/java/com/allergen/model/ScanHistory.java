package com.allergen.model;

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

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime removedAt;

    @Column
    private int allergenCount;

    @Column
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String productName;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        scannedAt = now;
        createdAt = now;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getDetectedAllergens() { return detectedAllergens; }
    public void setDetectedAllergens(String detectedAllergens) { this.detectedAllergens = detectedAllergens; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getRemovedAt() { return removedAt; }
    public void setRemovedAt(LocalDateTime removedAt) { this.removedAt = removedAt; }

    public int getAllergenCount() { return allergenCount; }
    public void setAllergenCount(int allergenCount) { this.allergenCount = allergenCount; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}

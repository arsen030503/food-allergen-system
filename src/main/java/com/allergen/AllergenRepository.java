package com.allergen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    List<Allergen> findByStandard(String standard);
    List<Allergen> findByNameContainingIgnoreCase(String name);
}
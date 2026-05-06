package com.allergen.repository;

import com.allergen.model.ScanHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {
    // FIX: история только для конкретного пользователя
    List<ScanHistory> findTop10ByUserIdOrderByScannedAtDesc(Long userId);
    List<ScanHistory> findTop10ByOrderByScannedAtDesc();

    long countByUserId(Long userId);

    List<ScanHistory> findByUserIdOrderByScannedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}

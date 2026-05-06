package com.allergen.repository;

import com.allergen.model.ScanHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {

    List<ScanHistory> findTop10ByUserIdAndRemovedAtIsNullOrderByScannedAtDesc(Long userId);

    long countByUserIdAndRemovedAtIsNull(Long userId);

    List<ScanHistory> findByUserIdAndRemovedAtIsNullOrderByScannedAtDesc(Long userId);

    Optional<ScanHistory> findByIdAndUserIdAndRemovedAtIsNull(Long id, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ScanHistory h SET h.removedAt = :ts WHERE h.userId = :userId AND h.removedAt IS NULL")
    int softDeleteAllForUser(@Param("userId") Long userId, @Param("ts") LocalDateTime ts);
}

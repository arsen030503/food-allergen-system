package com.allergen.repository;

import com.allergen.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndRemovedAtIsNull(String email);

    boolean existsByEmailAndRemovedAtIsNull(String email);

    List<User> findAllByRemovedAtIsNullOrderByIdAsc();

    Optional<User> findByIdAndRemovedAtIsNull(Long id);
}

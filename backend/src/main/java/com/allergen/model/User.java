package com.allergen.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column
    private String myAllergens = "";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime removedAt;

    @Column
    private LocalDateTime blockedAt;

    /**
     * Store as PostgreSQL BYTEA. Do not use {@code @Lob} on byte[] here: Hibernate maps that to {@code oid},
     * which conflicts with BYTEA columns and breaks updates (e.g. name change).
     */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "avatar_full")
    private byte[] avatarFull;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "avatar_thumb")
    private byte[] avatarThumb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMyAllergens() { return myAllergens; }
    public void setMyAllergens(String myAllergens) { this.myAllergens = myAllergens; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getRemovedAt() { return removedAt; }
    public void setRemovedAt(LocalDateTime removedAt) { this.removedAt = removedAt; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }

    public byte[] getAvatarFull() { return avatarFull; }
    public void setAvatarFull(byte[] avatarFull) { this.avatarFull = avatarFull; }

    public byte[] getAvatarThumb() { return avatarThumb; }
    public void setAvatarThumb(byte[] avatarThumb) { this.avatarThumb = avatarThumb; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}

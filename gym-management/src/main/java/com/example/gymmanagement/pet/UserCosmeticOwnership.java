package com.example.gymmanagement.pet;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_cosmetic_ownership",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cosmetic_code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserCosmeticOwnership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cosmetic_code", nullable = false)
    private String cosmeticCode; // tên enum CosmeticItem, vd "SHIRT_RED"

    private LocalDateTime purchasedAt;

    @PrePersist
    void onCreate() { if (purchasedAt == null) purchasedAt = LocalDateTime.now(); }
}
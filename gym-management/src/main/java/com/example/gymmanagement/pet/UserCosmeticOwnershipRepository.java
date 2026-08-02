package com.example.gymmanagement.pet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserCosmeticOwnershipRepository extends JpaRepository<UserCosmeticOwnership, Long> {
    List<UserCosmeticOwnership> findByUserId(Long userId);
    boolean existsByUserIdAndCosmeticCode(Long userId, String cosmeticCode);
}
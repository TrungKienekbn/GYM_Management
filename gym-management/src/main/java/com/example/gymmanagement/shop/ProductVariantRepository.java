package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdOrderByIdAsc(Long productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select v from ProductVariant v where v.id=:id") Optional<ProductVariant> findLocked(@Param("id") Long id);
}
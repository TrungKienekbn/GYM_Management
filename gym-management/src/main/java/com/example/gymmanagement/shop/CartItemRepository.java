package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface CartItemRepository extends JpaRepository<CartItem,Long>{
    List<CartItem> findByUserId(Long id);
    void deleteByUserId(Long id);
    @Query("select c from CartItem c where c.user.id=:userId and c.product.id=:productId and ((:variantId is null and c.variantId is null) or c.variantId=:variantId)")
    Optional<CartItem> findByUserIdAndProductIdAndVariantId(@Param("userId") Long userId, @Param("productId") Long productId, @Param("variantId") Long variantId);
}
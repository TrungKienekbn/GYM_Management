package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);
    boolean existsByOrderIdAndProductIdAndUserId(Long orderId, Long productId, Long userId);
}
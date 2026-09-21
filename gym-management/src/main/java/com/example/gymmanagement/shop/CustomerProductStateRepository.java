package com.example.gymmanagement.shop;
public interface CustomerProductStateRepository extends org.springframework.data.jpa.repository.JpaRepository<CustomerProductState,Long> { java.util.List<CustomerProductState> findByUserIdOrderByViewedAtDesc(Long userId); java.util.Optional<CustomerProductState> findByUserIdAndProductId(Long userId,Long productId); }

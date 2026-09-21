package com.example.gymmanagement.shop;
public interface ShopOrderEventRepository extends org.springframework.data.jpa.repository.JpaRepository<ShopOrderEvent,Long> { java.util.List<ShopOrderEvent> findByOrderIdOrderByIdAsc(Long orderId); }

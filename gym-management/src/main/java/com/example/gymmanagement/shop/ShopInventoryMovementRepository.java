package com.example.gymmanagement.shop;
public interface ShopInventoryMovementRepository extends org.springframework.data.jpa.repository.JpaRepository<ShopInventoryMovement,Long> { org.springframework.data.domain.Page<ShopInventoryMovement> findByProductId(Long id,org.springframework.data.domain.Pageable page); }

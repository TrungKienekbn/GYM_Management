package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CartItemRepository extends JpaRepository<CartItem,Long>{List<CartItem> findByUserId(Long id); Optional<CartItem> findByUserIdAndProductId(Long u,Long p); void deleteByUserId(Long id);}

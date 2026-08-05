package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.*; import java.util.*;
public interface StoreOrderRepository extends JpaRepository<StoreOrder,Long>{List<StoreOrder> findByUserIdOrderByCreatedAtDesc(Long id);List<StoreOrder> findAllByOrderByCreatedAtDesc();List<StoreOrder> findByStatusIn(List<OrderStatus>s);List<StoreOrder> findByStatusAndExpiresAtBefore(OrderStatus s,LocalDateTime t);}

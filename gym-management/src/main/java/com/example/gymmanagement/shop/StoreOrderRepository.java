package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.*; import java.util.*;
public interface StoreOrderRepository extends JpaRepository<StoreOrder,Long>{
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @org.springframework.data.jpa.repository.Query("select o from StoreOrder o where o.id=:id") java.util.Optional<StoreOrder> findLocked(@org.springframework.data.repository.query.Param("id")Long id);
 java.util.Optional<StoreOrder> findByGatewayReference(String reference);
 List<StoreOrder> findByUserIdOrderByCreatedAtDesc(Long id);
    List<StoreOrder> findAllByOrderByCreatedAtDesc();
    List<StoreOrder> findByStatusIn(List<OrderStatus>s);
    List<StoreOrder> findByStatusAndExpiresAtBefore(OrderStatus s,LocalDateTime t);
    List<StoreOrder> findByChannelOrderByCreatedAtDesc(SalesChannel channel);
    List<StoreOrder> findByCreatedByStaffIdAndChannelAndCreatedAtBetween(Long staffId, SalesChannel channel, LocalDateTime start, LocalDateTime end);
}
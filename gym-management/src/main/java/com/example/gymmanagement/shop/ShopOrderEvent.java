package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="shop_order_events")
@Getter @Setter @NoArgsConstructor
public class ShopOrderEvent { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long orderId; private String fromStatus; private String toStatus; private String actor; @Column(length=1000) private String note; private java.time.LocalDateTime createdAt; }

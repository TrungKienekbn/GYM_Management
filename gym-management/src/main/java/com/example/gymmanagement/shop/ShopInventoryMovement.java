package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="shop_inventory_movements")
@Getter @Setter @NoArgsConstructor
public class ShopInventoryMovement { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long productId; private Long variantId; private Long orderId; private int beforeStock; private int afterStock; private String actor; @Column(length=1000) private String reason; private java.time.LocalDateTime createdAt; }

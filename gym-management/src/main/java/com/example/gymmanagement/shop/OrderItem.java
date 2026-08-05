package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="shop_order_items") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id") private StoreOrder order;
 private Long productId; private String productName; private String imageUrl; private Double unitPrice; private Integer quantity; private Double lineTotal;
}

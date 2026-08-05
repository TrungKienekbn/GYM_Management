package com.example.gymmanagement.shop;
import com.example.gymmanagement.entity.User;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="shop_cart_items",uniqueConstraints=@UniqueConstraint(columnNames={"user_id","product_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private User user;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="product_id") private Product product;
 private Integer quantity;
}

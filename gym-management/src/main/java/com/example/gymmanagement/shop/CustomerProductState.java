package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="customer_product_states", uniqueConstraints=@UniqueConstraint(columnNames={"userId","productId"}))
@Getter @Setter @NoArgsConstructor
public class CustomerProductState { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long userId; private Long productId; private boolean wishlisted; private java.time.LocalDateTime viewedAt; }

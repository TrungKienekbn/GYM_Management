package com.example.gymmanagement.shop;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="shop_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String name;
 @Enumerated(EnumType.STRING) private ProductCategory category;
 @Column(length=2000) private String description;
 private String brand; private String imageUrl;
 @Column(nullable=false) private Double price;
 private Double salePrice;
 @Column(nullable=false) private Integer stock;
 @Builder.Default private Boolean active=true;
 private String suitableGoals; private String requiredEquipmentCode;
 @Builder.Default private LocalDateTime createdAt=LocalDateTime.now();
}

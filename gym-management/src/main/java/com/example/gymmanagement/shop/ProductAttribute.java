package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="product_attributes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductAttribute {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String name;
}
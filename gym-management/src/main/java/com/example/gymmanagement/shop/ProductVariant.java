package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
import java.util.*;

@Entity @Table(name="product_variants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariant {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
    private String sku;
    @Column(nullable=false) private Integer stock;
    private Double priceOverride;
    @Builder.Default private Boolean active = true;
    @ManyToMany
    @JoinTable(name="variant_attribute_values",
            joinColumns=@JoinColumn(name="variant_id"),
            inverseJoinColumns=@JoinColumn(name="value_id"))
    @Builder.Default private Set<ProductAttributeValue> attributeValues = new HashSet<>();
}
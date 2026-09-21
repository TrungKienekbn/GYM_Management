package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity @Table(name="vouchers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Voucher {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(unique=true) private String code;
    private String description;
    @Enumerated(EnumType.STRING) private VoucherType type;
    private Double value;
    private Double minOrderAmount;
    private Double maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @Builder.Default private Boolean active = true;
    @Enumerated(EnumType.STRING) @Builder.Default private VoucherScopeType scopeType = VoucherScopeType.ALL;
    @Enumerated(EnumType.STRING) private ProductCategory scopeCategory;
    @ElementCollection
    @CollectionTable(name="voucher_scope_products", joinColumns=@JoinColumn(name="voucher_id"))
    @Column(name="product_id")
    private Set<Long> scopeProductIds;
    private LocalDateTime createdAt;
    @PrePersist void init(){ if(createdAt==null) createdAt=LocalDateTime.now(); if(usedCount==null) usedCount=0; }
}
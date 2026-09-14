package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    @PrePersist void init(){ if(createdAt==null) createdAt=LocalDateTime.now(); if(usedCount==null) usedCount=0; }
}
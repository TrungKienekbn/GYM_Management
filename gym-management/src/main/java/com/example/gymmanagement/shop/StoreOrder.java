package com.example.gymmanagement.shop;
import com.example.gymmanagement.entity.User;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime; import java.util.*;
@Entity @Table(name="shop_orders") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private User user;
 @Enumerated(EnumType.STRING) private OrderStatus status;
 private Double subtotal; private Double discount; private Double shippingFee; private Double total;
 private String receiverName; private String phone; @Column(length=1000) private String shippingAddress;
 @Column(length=1000) private String note;
 @Column(unique=true) private String transferCode; @Column(length=1000) private String qrRawPayload; private String qrCodeUrl;
 private String transactionId; private LocalDateTime createdAt; private LocalDateTime expiresAt; private LocalDateTime paidAt;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) @Builder.Default private List<OrderItem> items=new ArrayList<>();
 @PrePersist void init(){if(createdAt==null)createdAt=LocalDateTime.now();}
}

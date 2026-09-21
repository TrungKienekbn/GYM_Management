package com.example.gymmanagement.shop;
import com.example.gymmanagement.entity.User;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="product_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductReview {
 @Convert(converter=ShopImageListConverter.class) @Column(length=20000) private java.util.List<String> images;
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private User user;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id") private StoreOrder order;
    private Integer rating;
    @Column(length=2000) private String comment;
    private LocalDateTime createdAt;
    @PrePersist void init(){ if(createdAt==null) createdAt=LocalDateTime.now(); }
}
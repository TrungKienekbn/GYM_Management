package com.example.gymmanagement.shop;

import com.example.gymmanagement.entity.User;
import com.example.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductReviewService {
    private final ProductReviewRepository reviews;
    private final ProductRepository products;
    private final StoreOrderRepository orders;
    private final UserRepository users;

    @Transactional
    public Map<String, Object> create(String email, Long orderId, Long productId, Integer rating, String comment, List<String> images) {
        if (rating == null || rating < 1 || rating > 5) throw new RuntimeException("Số sao đánh giá phải từ 1 đến 5");
        User u = users.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        StoreOrder o = orders.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (o.getUser() == null || !o.getUser().getId().equals(u.getId())) throw new RuntimeException("Đây không phải đơn hàng của bạn");
        if (!canReview(o)) throw new RuntimeException("Chỉ có thể đánh giá sau khi đơn hàng đã hoàn thành/nhận hàng");
        boolean inOrder = o.getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
        if (!inOrder) throw new RuntimeException("Sản phẩm không thuộc đơn hàng này");
        if (reviews.existsByOrderIdAndProductIdAndUserId(orderId, productId, u.getId())) throw new RuntimeException("Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi");
        Product p = products.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        ProductReview r = ProductReview.builder().product(p).user(u).order(o).rating(rating).comment(comment).images(ShopImageListConverter.validate(images)).build();
        return map(reviews.save(r));
    }

    public List<Map<String, Object>> forProduct(Long productId) {
        return reviews.findByProductIdOrderByCreatedAtDesc(productId).stream().map(this::map).toList();
    }

    public double[] summary(Long productId) {
        List<ProductReview> list = reviews.findByProductIdOrderByCreatedAtDesc(productId);
        double sum = list.stream().mapToInt(ProductReview::getRating).sum();
        return new double[]{sum, list.size()};
    }

    private boolean canReview(StoreOrder o) {
        if (o.getChannel() == SalesChannel.POS) return o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.COMPLETED;
        return o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.COMPLETED;
    }

    private Map<String, Object> map(ProductReview r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("productId", r.getProduct().getId());
        m.put("userName", r.getUser().getFullName());
        m.put("rating", r.getRating());
        m.put("comment", r.getComment());m.put("images",r.getImages()==null?List.of():r.getImages());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }
}
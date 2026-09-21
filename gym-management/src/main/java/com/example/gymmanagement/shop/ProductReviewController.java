package com.example.gymmanagement.shop;

import com.example.gymmanagement.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService reviewService;

    @GetMapping("/products/{id}/reviews")
    public ResponseEntity<?> list(@PathVariable Long id,@RequestParam(required=false)Integer page,@RequestParam(defaultValue="6")int size,@RequestParam(required=false)Integer rating) {
        var list=reviewService.forProduct(id).stream().filter(r->rating==null||rating.equals(r.get("rating"))).toList();return ResponseEntity.ok(ApiResponse.success(page==null?list:ShopExperienceController.page(list,page,size)));
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails u, @RequestBody Map<String, Object> b) {
        Long orderId = Long.valueOf(String.valueOf(b.get("orderId")));
        Long productId = Long.valueOf(String.valueOf(b.get("productId")));
        Integer rating = Integer.valueOf(String.valueOf(b.get("rating")));
        String comment = b.get("comment") == null ? null : String.valueOf(b.get("comment"));
        return ResponseEntity.ok(ApiResponse.success(reviewService.create(u.getUsername(), orderId, productId, rating, comment, b.get("images") instanceof java.util.List<?> images ? images.stream().map(String::valueOf).toList() : java.util.List.of()), "Đã gửi đánh giá"));
    }
}
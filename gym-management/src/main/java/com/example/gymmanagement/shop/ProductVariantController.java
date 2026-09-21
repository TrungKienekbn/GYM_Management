package com.example.gymmanagement.shop;

import com.example.gymmanagement.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductVariantService variantService;

    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<?> list(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(variantService.forProduct(productId)));
    }

    @PostMapping("/admin/products/{productId}/variants")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> create(@PathVariable Long productId, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(ApiResponse.success(variantService.create(productId, b), "Đã tạo biến thể"));
    }

    @PutMapping("/admin/products/variants/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(ApiResponse.success(variantService.update(id, b), "Đã cập nhật biến thể"));
    }

    @DeleteMapping("/admin/products/variants/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        variantService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa biến thể"));
    }
}
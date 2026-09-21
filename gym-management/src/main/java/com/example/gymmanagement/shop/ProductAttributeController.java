package com.example.gymmanagement.shop;

import com.example.gymmanagement.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop/admin/attributes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ProductAttributeController {
    private final ProductAttributeService service;

    @GetMapping
    public ResponseEntity<?> all() { return ResponseEntity.ok(ApiResponse.success(service.allAttributes())); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> b) { return ResponseEntity.ok(ApiResponse.success(service.createAttribute(String.valueOf(b.get("name"))), "Đã tạo thuộc tính")); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) { service.deleteAttribute(id); return ResponseEntity.ok(ApiResponse.success("Đã xóa thuộc tính")); }

    @PostMapping("/{id}/values")
    public ResponseEntity<?> addValue(@PathVariable Long id, @RequestBody Map<String, Object> b) { return ResponseEntity.ok(ApiResponse.success(service.addValue(id, String.valueOf(b.get("value"))), "Đã thêm giá trị")); }

    @DeleteMapping("/values/{id}")
    public ResponseEntity<?> deleteValue(@PathVariable Long id) { service.deleteValue(id); return ResponseEntity.ok(ApiResponse.success("Đã xóa giá trị")); }
}
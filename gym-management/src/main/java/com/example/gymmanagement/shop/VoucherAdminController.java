package com.example.gymmanagement.shop;

import com.example.gymmanagement.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class VoucherAdminController {
    private final VoucherService voucherService;

    @GetMapping public ResponseEntity<?> all() { return ResponseEntity.ok(ApiResponse.success(voucherService.all())); }
    @PostMapping public ResponseEntity<?> create(@RequestBody Voucher v) { return ResponseEntity.ok(ApiResponse.success(voucherService.save(null, v), "Đã tạo voucher")); }
    @PutMapping("/{id}") public ResponseEntity<?> edit(@PathVariable Long id, @RequestBody Voucher v) { return ResponseEntity.ok(ApiResponse.success(voucherService.save(id, v), "Đã cập nhật voucher")); }
    @DeleteMapping("/{id}") public ResponseEntity<?> remove(@PathVariable Long id) { voucherService.remove(id); return ResponseEntity.ok(ApiResponse.success("Đã ngừng áp dụng voucher")); }
}
package com.example.gymmanagement.shop;

import com.example.gymmanagement.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop/pos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STAFF')")
public class PosController {
    private final PosService pos;

    @PostMapping("/orders")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails u, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(pos.createOrder(u.getUsername(), body), "Bán hàng thành công"));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(ApiResponse.success(pos.posOrders()));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pos.posOrder(id)));
    }
}
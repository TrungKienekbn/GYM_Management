package com.example.gymmanagement.shop;
import com.example.gymmanagement.dto.response.ApiResponse; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.core.userdetails.UserDetails; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/shop") @RequiredArgsConstructor
public class ShopController { private final ShopService shop;
 @GetMapping("/products") public ResponseEntity<?> products(@AuthenticationPrincipal UserDetails u,@RequestParam(required=false)String category,@RequestParam(required=false)String keyword){return ResponseEntity.ok(ApiResponse.success(shop.products(u.getUsername(),category,keyword)));}
 @GetMapping("/cart") public ResponseEntity<?> cart(@AuthenticationPrincipal UserDetails u){return ResponseEntity.ok(ApiResponse.success(shop.cart(u.getUsername())));}
 @PostMapping("/cart") public ResponseEntity<?> add(@AuthenticationPrincipal UserDetails u,@RequestBody Map<String,Object>b){shop.addCart(u.getUsername(),Long.valueOf(String.valueOf(b.get("productId"))),Integer.parseInt(String.valueOf(b.getOrDefault("quantity",1))));return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ"));}
 @PutMapping("/cart/{id}") public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails u,@PathVariable Long id,@RequestBody Map<String,Object>b){shop.updateCart(u.getUsername(),id,Integer.parseInt(String.valueOf(b.get("quantity"))));return ResponseEntity.ok(ApiResponse.success("Đã cập nhật"));}
 @DeleteMapping("/cart/{id}") public ResponseEntity<?> remove(@AuthenticationPrincipal UserDetails u,@PathVariable Long id){shop.removeCart(u.getUsername(),id);return ResponseEntity.ok(ApiResponse.success("Đã xóa"));}
 @PostMapping("/orders") public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails u,@RequestBody Map<String,Object>b){return ResponseEntity.ok(ApiResponse.success(shop.checkout(u.getUsername(),b)));}
 @GetMapping("/orders") public ResponseEntity<?> orders(@AuthenticationPrincipal UserDetails u){return ResponseEntity.ok(ApiResponse.success(shop.myOrders(u.getUsername())));}
 @GetMapping("/orders/{id}") public ResponseEntity<?> order(@AuthenticationPrincipal UserDetails u,@PathVariable Long id){return ResponseEntity.ok(ApiResponse.success(shop.order(u.getUsername(),id)));}
 @PostMapping("/orders/{id}/cancel") public ResponseEntity<?> cancel(@AuthenticationPrincipal UserDetails u,@PathVariable Long id){return ResponseEntity.ok(ApiResponse.success(shop.cancel(u.getUsername(),id)));}
 @GetMapping("/admin/products") @PreAuthorize("hasAuthority('ROLE_ADMIN')") public ResponseEntity<?> all(){return ResponseEntity.ok(ApiResponse.success(shop.allProducts()));}
 @PostMapping("/admin/products") @PreAuthorize("hasAuthority('ROLE_ADMIN')") public ResponseEntity<?> create(@RequestBody Product p){return ResponseEntity.ok(ApiResponse.success(shop.saveProduct(null,p)));}
 @PutMapping("/admin/products/{id}") @PreAuthorize("hasAuthority('ROLE_ADMIN')") public ResponseEntity<?> edit(@PathVariable Long id,@RequestBody Product p){return ResponseEntity.ok(ApiResponse.success(shop.saveProduct(id,p)));}
 @DeleteMapping("/admin/products/{id}") @PreAuthorize("hasAuthority('ROLE_ADMIN')") public ResponseEntity<?> hide(@PathVariable Long id){shop.hideProduct(id);return ResponseEntity.ok(ApiResponse.success("Đã ngừng bán"));}
 @GetMapping("/admin/orders") @PreAuthorize("hasAuthority('ROLE_ADMIN')") public ResponseEntity<?> allOrders(){return ResponseEntity.ok(ApiResponse.success(shop.allOrders()));}
 @PutMapping("/admin/orders/{id}/status") @PreAuthorize("hasAuthority('ROLE_ADMIN')") public ResponseEntity<?> status(@PathVariable Long id,@RequestBody Map<String,String>b){return ResponseEntity.ok(ApiResponse.success(shop.updateStatus(id,OrderStatus.valueOf(b.get("status")))));}
}

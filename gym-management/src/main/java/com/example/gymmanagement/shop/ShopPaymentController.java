package com.example.gymmanagement.shop;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.core.userdetails.UserDetails; import org.springframework.http.ResponseEntity; import com.example.gymmanagement.dto.response.ApiResponse; import java.util.*;
@RestController @RequestMapping("/api/shop/payments") @RequiredArgsConstructor public class ShopPaymentController {
 private final ShopPaymentService payments;
 @GetMapping("/methods") public Object methods(){return ApiResponse.success(payments.methods());}
 @PostMapping("/{id}/start") public Object start(@AuthenticationPrincipal UserDetails u,@PathVariable Long id){return ApiResponse.success(payments.start(u.getUsername(),id));}
 @PostMapping("/momo/ipn") public Object momo(@RequestBody Map<String,Object> b){payments.momo(b);return ResponseEntity.noContent().build();}
 @PostMapping("/zalopay/callback") public Object zalo(@RequestBody Map<String,Object> b){try{payments.zalo(b);return Map.of("return_code",1,"return_message","success");}catch(Exception e){return Map.of("return_code",0,"return_message","Callback chưa được chấp nhận");}}
}

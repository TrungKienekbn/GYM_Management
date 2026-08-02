package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.request.LoginRequest;
import com.example.gymmanagement.dto.request.RegisterRequest;
import com.example.gymmanagement.dto.request.PhoneLast4LoginRequest;
import com.example.gymmanagement.dto.response.ApiResponse;
import com.example.gymmanagement.dto.response.AuthResponse;
import com.example.gymmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request), "Registration successful! Please verify your email."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request), "Login successful"));
    }

    @PostMapping("/login-phone-last4")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithPhoneLast4(@RequestBody PhoneLast4LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.loginWithPhoneLast4(request), "Xác minh thành công"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyEmail(token), "Email verified"));
    }
}

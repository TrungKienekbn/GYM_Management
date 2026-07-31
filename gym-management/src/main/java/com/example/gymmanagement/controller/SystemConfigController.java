package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.request.SystemConfigUpdateRequest;
import com.example.gymmanagement.dto.response.ApiResponse;
import com.example.gymmanagement.entity.SystemConfig;
import com.example.gymmanagement.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// /api/admin/** đã được chặn ROLE_ADMIN sẵn trong SecurityConfig -> không cần thêm gì.
@RestController
@RequestMapping("/api/admin/system-configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(systemConfigService.getAll()));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfig>> update(
            @PathVariable String key, @RequestBody SystemConfigUpdateRequest req) {
        if (req.getValue() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Giá trị không được để trống"));
        }
        SystemConfig updated = systemConfigService.update(key, req.getValue());
        return ResponseEntity.ok(ApiResponse.success(updated, "Đã cập nhật công thức!"));
    }
}
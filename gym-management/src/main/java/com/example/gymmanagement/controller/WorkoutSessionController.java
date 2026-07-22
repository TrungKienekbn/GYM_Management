package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.request.*;
import com.example.gymmanagement.dto.response.*;
import com.example.gymmanagement.service.WorkoutSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService sessionService;

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> enroll(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody EnrollSessionRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.enrollSession(ud.getUsername(), req), "Đã đăng ký lịch tập!"));
    }

    @GetMapping("/week-progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> weekProgress(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam Long planId, @RequestParam Integer weekNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.getWeekProgress(ud.getUsername(), planId, weekNumber)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkoutSessionResponse>>> getAll(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getMySessions(ud.getUsername())));
    }

    @GetMapping("/this-week")
    public ResponseEntity<ApiResponse<List<WorkoutSessionResponse>>> getWeek(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getWeekSessions(ud.getUsername())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> getOne(
            @AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sessionService.getSessionById(ud.getUsername(), id)));
    }

    @GetMapping("/order-check")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkOrder(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam Long planDayId,
            @RequestParam Integer weekNumber,
            @RequestParam String sessionDate) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.checkOrderWarning(ud.getUsername(), planDayId, weekNumber,
                        java.time.LocalDate.parse(sessionDate))));
    }

    // ── XOÁ (Patch 10): endpoint /check-in đã bị bỏ hoàn toàn khỏi nghiệp vụ ──

    // Checkout dùng CHUNG 1 API cho cả 2 lần gọi (Business Rule mục 13, LOCKED):
    // - Lần 1 (chỉ exerciseLogs): nếu là Last Completed Session -> không lưu gì,
    //   trả needWeeklyReview=true.
    // - Lần 2 (exerciseLogs + checkoutWeight + assessment nếu ENDURANCE): lưu toàn bộ.
    @PostMapping("/{id}/check-out")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> checkOut(
            @AuthenticationPrincipal UserDetails ud, @PathVariable Long id,
            @RequestBody CheckOutRequest req) {
        WorkoutSessionResponse resp = sessionService.checkOut(ud.getUsername(), id, req);
        String message = Boolean.TRUE.equals(resp.getNeedWeeklyReview())
                ? "Cần hoàn tất Review cuối tuần trước khi Checkout"
                : "Check-out thành công! 🎉";
        return ResponseEntity.ok(ApiResponse.success(resp, message));
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> skip(
            @AuthenticationPrincipal UserDetails ud, @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.skipSession(ud.getUsername(), id,
                        body != null ? body.get("notes") : null)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails ud, @PathVariable Long id) {
        sessionService.deleteSession(ud.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa lịch tập"));
    }
}
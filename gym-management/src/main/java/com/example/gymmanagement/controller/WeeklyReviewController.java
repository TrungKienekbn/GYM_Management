package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.request.WeeklyReviewRequest;
import com.example.gymmanagement.dto.response.ApiResponse;
import com.example.gymmanagement.dto.response.WeeklyReviewResponse;
import com.example.gymmanagement.service.WeeklyReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weekly-reviews")
@RequiredArgsConstructor
public class WeeklyReviewController {

    private final WeeklyReviewService weeklyReviewService;

    @GetMapping("/eligibility")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEligibility(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long planId,
            @RequestParam Integer weekNumber) {
        boolean eligible = weeklyReviewService.isEligible(userDetails.getUsername(), planId, weekNumber);
        return ResponseEntity.ok(ApiResponse.success(Map.of("eligible", eligible)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WeeklyReviewResponse>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WeeklyReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                weeklyReviewService.submit(userDetails.getUsername(), request),
                "Cảm ơn bạn đã đánh giá tuần tập luyện!"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<WeeklyReviewResponse>>> getMy(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(weeklyReviewService.getMyReviews(userDetails.getUsername())));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<WeeklyReviewResponse>>> getAllAdmin() {
        return ResponseEntity.ok(ApiResponse.success(weeklyReviewService.getAllReviews()));
    }
}
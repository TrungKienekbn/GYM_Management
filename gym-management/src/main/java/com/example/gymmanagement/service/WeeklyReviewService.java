package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.request.WeeklyReviewRequest;
import com.example.gymmanagement.dto.response.WeeklyReviewResponse;
import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.SessionStatus;
import com.example.gymmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyReviewService {

    private final WeeklyReviewRepository weeklyReviewRepository;
    private final WorkoutPlanRepository planRepository;
    private final WorkoutSessionRepository sessionRepository;
    private final UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /** User đã checkout buổi cuối của tuần này (có nhập checkoutWeight) và chưa đánh giá tuần đó. */
    public boolean isEligible(String email, Long planId, Integer weekNumber) {
        User user = getUser(email);
        boolean lastCheckedOut = sessionRepository.findLastSessionOfWeek(user.getId(), planId, weekNumber)
                .stream().anyMatch(s -> s.getStatus() == SessionStatus.COMPLETED && s.getCheckoutWeight() != null);
        if (!lastCheckedOut) return false;
        return !weeklyReviewRepository.existsByUserIdAndWorkoutPlanIdAndWeekNumber(user.getId(), planId, weekNumber);
    }

    @Transactional
    public WeeklyReviewResponse submit(String email, WeeklyReviewRequest req) {
        User user = getUser(email);
        WorkoutPlan plan = planRepository.findById(req.getPlanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo án"));

        if (!isEligible(email, req.getPlanId(), req.getWeekNumber())) {
            throw new RuntimeException("Bạn chưa hoàn thành tuần này hoặc đã đánh giá rồi");
        }
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new RuntimeException("Vui lòng chọn từ 1 đến 5 sao");
        }

        WeeklyReview review = WeeklyReview.builder()
                .user(user).workoutPlan(plan)
                .weekNumber(req.getWeekNumber())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        weeklyReviewRepository.save(review);
        return toResponse(review);
    }

    public List<WeeklyReviewResponse> getMyReviews(String email) {
        User user = getUser(email);
        return weeklyReviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<WeeklyReviewResponse> getAllReviews() {
        return weeklyReviewRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public WeeklyReviewResponse toResponse(WeeklyReview r) {
        return WeeklyReviewResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getFullName())
                .planId(r.getWorkoutPlan() != null ? r.getWorkoutPlan().getId() : null)
                .planName(r.getWorkoutPlan() != null ? r.getWorkoutPlan().getPlanName() : null)
                .weekNumber(r.getWeekNumber())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
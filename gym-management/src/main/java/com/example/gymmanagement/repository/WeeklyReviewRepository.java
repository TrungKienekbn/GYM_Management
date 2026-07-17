package com.example.gymmanagement.repository;

import com.example.gymmanagement.entity.WeeklyReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyReviewRepository extends JpaRepository<WeeklyReview, Long> {
    boolean existsByUserIdAndWorkoutPlanIdAndWeekNumber(Long userId, Long workoutPlanId, Integer weekNumber);
    List<WeeklyReview> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<WeeklyReview> findAllByOrderByCreatedAtDesc();
}
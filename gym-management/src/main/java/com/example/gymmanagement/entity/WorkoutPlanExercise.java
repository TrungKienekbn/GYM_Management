package com.example.gymmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workout_plan_exercises")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutPlanExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_day_id")
    private WorkoutPlanDay planDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    private Integer sets;
    private Integer reps;
    private Integer durationSeconds;
    private Integer restSeconds;
    private Integer orderIndex;
    private String notes;

    // ── Tạ khởi điểm (nhập 1 lần) + tạ hiện tại (auto tính theo tuần) ──
    private Double baseWeightKg;
    private Double currentWeightKg;
    private Integer weightUpdatedWeek;

    // ── Mức tạ khuyến nghị — SNAPSHOT AI, tính đúng 1 lần khi sinh giáo án, KHÔNG BAO GIỜ
    // thay đổi sau đó (LOCKED) ──
    private Double recommendedWeightKg;

    // ── MỚI: Current Recommendation — chỉ có ý nghĩa khi baseWeightKg == null.
    // Công thức: roundToHalfKg(recommendedWeightKg × multiplier hiện tại), LUÔN tính lại
    // từ recommendedWeightKg gốc, KHÔNG tích luỹ chồng lên giá trị tuần trước.
    // Đóng băng (ngừng cập nhật) vĩnh viễn kể từ khi baseWeightKg != null.
    private Double currentRecommendedWeightKg;
}
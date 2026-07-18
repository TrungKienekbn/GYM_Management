package com.example.gymmanagement.dto.response;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutPlanExerciseResponse {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private String muscleGroup;
    private String difficulty;
    private Integer sets;
    private Integer reps;
    private Integer durationSeconds;
    private Integer restSeconds;
    private Integer orderIndex;
    private String notes;
    private String videoUrl;
    private Integer caloriesBurned;

    private Double baseWeightKg;
    private Double currentWeightKg;
    private Boolean weightJustRevealed;

    private Double recommendedWeightKg;

    // ── MỚI: Current Recommendation — chỉ có giá trị khi baseWeightKg == null ──
    private Double currentRecommendedWeightKg;
}
package com.example.gymmanagement.entity;

import com.example.gymmanagement.enums.Difficulty;
import com.example.gymmanagement.enums.MuscleGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import com.example.gymmanagement.enums.AssessmentMetricType;

@Entity
@Table(name = "exercises")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String videoUrl;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private MuscleGroup muscleGroup;

    /** CSV các nhóm cơ tham gia hỗ trợ, ví dụ: SHOULDERS,ARMS. */
    private String secondaryMuscleGroups;

    /** CSV vùng chấn thương mà bài tập không phù hợp, ví dụ: SHOULDER,WRIST. */
    private String contraindicatedInjuries;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Integer caloriesBurned;
    private Integer defaultSets;
    private Integer defaultReps;
    private Integer defaultDurationSeconds;
    private Integer restSeconds;

    @Builder.Default
    private Integer muscleGainScore   = 0;

    @Builder.Default
    private Integer weightLossScore   = 0;

    @Builder.Default
    private Integer enduranceScore    = 0;

    @Builder.Default
    private Integer flexibilityScore  = 0;

    @Builder.Default
    private Integer maintenanceScore  = 0;

    @ColumnDefault("10")
    @Builder.Default
    private Integer staminaCost = 10;

    @Builder.Default
    private Boolean isActive = true;

    private Boolean usesWeight;

    @Enumerated(EnumType.STRING)
    private AssessmentMetricType assessmentMetricType; // null = bài tập thường

    @Builder.Default
    private Boolean isAssessment = false;

    // đảm bảo 2 field luôn khớp nhau, không lệch dữ liệu do set tay sai
    @PrePersist @PreUpdate
    private void syncAssessmentFlag() {
        this.isAssessment = (this.assessmentMetricType != null);
    }
}

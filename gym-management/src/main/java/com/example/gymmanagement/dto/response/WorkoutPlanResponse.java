package com.example.gymmanagement.dto.response;

import com.example.gymmanagement.enums.FitnessLevel;
import com.example.gymmanagement.enums.Goal;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutPlanResponse {
    private Long   id;
    private String planName;
    private String description;
    private Goal   goal;
    private FitnessLevel targetLevel;
    private Integer durationWeeks;
    private Integer sessionsPerWeek;
    private Integer currentWeek;
    private Boolean isActive;
    private Boolean isAiGenerated;
    private Boolean isTemplate;
    private Boolean isCompleted;
    private LocalDate  weekStartDate;
    private LocalDateTime createdAt;

    private Double startingBmi;
    private Double startingWeight;

    private Integer difficultyAdjustment;
    private Integer setsAdjustment;
    private Integer repsAdjustment;

    private List<WorkoutPlanDayResponse> planDays;

    private List<Integer> suggestedDays;
    private String                   scheduleNote;

    private String weightAdjustmentNote;

    private Integer enrolledThisWeek;
    private Integer completedThisWeek;

    private Integer maxMana;
    private Integer currentMana;
    private String manaMessage;

    private Integer fitnessScore;
    private String  fitnessLevel;
    private String  bodyType;

    private String  targetMetricType;
    private Double  targetBaselineValue;
    private Double  targetGoalValue;
    private Double  targetCurrentValue;
    private Boolean targetAchieved;

    // ── MỚI (Patch 7): thời lượng ước tính ban đầu, cố định, dùng làm mẫu số %thời gian ──
    private Integer estimatedWeeks;

    // ── MỚI: Giáo án nâng cao thể lực ──
    private Long originalPlanId;
    private Boolean isFitnessImprovement;
    private Integer requiredMaxSessionManaCost;

    // Cảnh báo mềm cho gói thường khi 2 tuần liên tiếp hoàn thành dưới 40%.
    private Boolean lowCompletionWarning;
    private Integer lowCompletionWeek1;
    private Integer lowCompletionRate1;
    private Integer lowCompletionWeek2;
    private Integer lowCompletionRate2;
    private String lowCompletionMessage;
}

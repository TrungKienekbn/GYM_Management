package com.example.gymmanagement.dto.response;

import com.example.gymmanagement.enums.SessionStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkoutSessionResponse {
    private Long   id;
    private LocalDate     sessionDate;
    private LocalTime     scheduledTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private SessionStatus status;
    private Integer totalCaloriesBurned;
    private Integer durationMinutes;
    private String  notes;
    private Integer weekNumber;
    private String  planName;
    private Long planId;
    private String  dayName;
    private String  customSessionName;
    private Boolean isCustom;
    private Integer completionRate;
    private Boolean isLastSessionOfWeek;
    private String dayMismatchWarning;

    @Builder.Default
    private Boolean scheduleSelectionRequired = false;
    private List<List<Integer>> scheduleOptions;

    private Double  checkoutWeight;
    private Double  checkoutBodyFat;
    private List<ExerciseLogResponse>         exerciseLogs;
    private List<WorkoutPlanExerciseResponse> planExercises;

    private Boolean injuryRisk;

    // ── MỚI (Patch 10) ──
    // Trả về ở lần gọi enrollSession() khi buổi được mở không đúng thứ tự đề xuất.
    private String orderWarning;
    // Trả về ở checkOut() khi tổng thực tế > 150% kế hoạch — chỉ cảnh báo, không chặn.
    private String overLimitWarning;
    // true = đây là Last Completed Session và LẦN GỌI NÀY CHƯA lưu gì cả,
    // FE phải mở Popup Review rồi gọi lại chính API Checkout với đầy đủ dữ liệu.
    @Builder.Default
    private Boolean needWeeklyReview = false;
}
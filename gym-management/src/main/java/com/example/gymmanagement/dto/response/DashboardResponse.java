package com.example.gymmanagement.dto.response;
import lombok.*;
import java.util.Map;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {
    // User stats
    private Long totalSessions;
    private Long completedSessions;
    private Long totalCaloriesBurned;
    private Double currentWeight;
    private Double startingWeight;
    private Double weightChange;
    private Double currentBmi;
    private Integer currentStreak;
    private Integer longestStreak;
    // Weekly progress
    private Map<String, Integer> weeklyCalories;
    private Map<String, Integer> weeklyWorkouts;
    // Admin stats
    private Long totalUsers;
    private Long activeUsers;
    private Double totalRevenue;
    private Double monthlyRevenue;
    private Long totalWorkoutPlans;
    private Double averageRating;
    // MỚI: thời gian tập & khối lượng % theo ngày/tuần
    private Map<String, Integer> dailyDuration;        // phút tập, theo từng ngày trong tuần hiện tại
    private Map<String, Integer> dailyVolumePercent;   // % hoàn thành trung bình, theo từng ngày trong tuần hiện tại
    private Map<String, Integer> weeklyDuration;        // tổng phút tập, 4 tuần gần nhất
    private Map<String, Integer> weeklyVolumePercent;   // % hoàn thành trung bình, 4 tuần gần nhất
    private Long totalDurationMinutes;                  // tổng phút tập từ trước đến giờ
    // MỚI: mục tiêu (yêu cầu) tương ứng để so sánh 2 thanh
    private Map<String, Integer> dailyDurationTarget;
    private Map<String, Integer> weeklyDurationTarget;
    private Map<String, Integer> dailyVolumeTarget;
    private Map<String, Integer> weeklyVolumeTarget;
}
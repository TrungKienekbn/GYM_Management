package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.response.DashboardResponse;
import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.SessionStatus;
import com.example.gymmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final WorkoutSessionRepository sessionRepository;
    private final ProgressTrackingRepository progressRepository;
    private final MembershipRepository membershipRepository;
    private final ServiceRatingRepository ratingRepository;
    private final WorkoutPlanRepository planRepository;

    public DashboardResponse getUserDashboard(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Long uid = user.getId();

        List<WorkoutSession> allSessions = sessionRepository.findByUserIdOrderBySessionDateDesc(uid);
        long totalSessions     = allSessions.size();
        long completedSessions = allSessions.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();

        Long totalCal = sessionRepository.sumCaloriesByUserId(uid);

        List<ProgressTracking> progList = progressRepository.findByUserIdOrderByDateAsc(uid);
        Double startWeight   = progList.isEmpty() ? null : progList.get(0).getWeight();
        Double currentWeight = progList.isEmpty() ? null : progList.get(progList.size() - 1).getWeight();
        Double currentBmi    = progList.isEmpty() ? null : progList.get(progList.size() - 1).getBmi();
        Double weightChange  = (startWeight != null && currentWeight != null)
                ? Math.round((currentWeight - startWeight) * 10.0) / 10.0 : null;

        int[] streaks = calculateStreaks(allSessions);

        Map<String, Integer> weeklyCalories = getWeeklyCalories(uid);
        Map<String, Integer> weeklyWorkouts = getWeeklyWorkouts(uid);

        Long totalDuration = allSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getDurationMinutes() != null)
                .mapToLong(WorkoutSession::getDurationMinutes).sum();

        Map<String, Integer> dailyDuration        = getDailyDuration(uid);
        Map<String, Integer> dailyVolumePercent   = getDailyVolumePercent(uid);
        Map<String, Integer> weeklyDuration       = getWeeklyDuration(uid);
        Map<String, Integer> weeklyVolumePercent  = getWeeklyVolumePercent(uid);
        Map<String, Integer> dailyDurationTarget  = getDailyDurationTarget(uid);
        Map<String, Integer> weeklyDurationTarget = getWeeklyDurationTarget(uid);
        Map<String, Integer> dailyVolumeTarget    = getDailyVolumeTarget(uid);
        Map<String, Integer> weeklyVolumeTarget   = getWeeklyVolumeTarget(uid);

        return DashboardResponse.builder()
                .totalSessions(totalSessions)
                .completedSessions(completedSessions)
                .totalCaloriesBurned(totalCal)
                .currentWeight(currentWeight)
                .startingWeight(startWeight)
                .weightChange(weightChange)
                .currentBmi(currentBmi)
                .currentStreak(streaks[0])
                .longestStreak(streaks[1])
                .weeklyCalories(weeklyCalories)
                .weeklyWorkouts(weeklyWorkouts)
                .totalDurationMinutes(totalDuration)
                .dailyDuration(dailyDuration)
                .dailyVolumePercent(dailyVolumePercent)
                .weeklyDuration(weeklyDuration)
                .weeklyVolumePercent(weeklyVolumePercent)
                .dailyDurationTarget(dailyDurationTarget)
                .weeklyDurationTarget(weeklyDurationTarget)
                .dailyVolumeTarget(dailyVolumeTarget)
                .weeklyVolumeTarget(weeklyVolumeTarget)
                .build();
    }

    public DashboardResponse getAdminDashboard() {
        long totalUsers  = userRepository.count();
        long activeUsers = userRepository.findAllActiveUsers().size();
        LocalDateTime now        = LocalDateTime.now();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime yearStart  = LocalDate.now().withDayOfYear(1).atStartOfDay();

        Double totalRevenue   = membershipRepository.sumRevenueBetween(LocalDate.of(2020,1,1).atStartOfDay(), now);
        Double monthlyRevenue = membershipRepository.sumRevenueBetween(monthStart, now);

        return DashboardResponse.builder()
                .totalUsers(totalUsers).activeUsers(activeUsers)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : 0.0)
                .totalWorkoutPlans(planRepository.count())
                .build();
    }

    private int[] calculateStreaks(List<WorkoutSession> allSessions) {
        Set<LocalDate> doneSet = new HashSet<>();
        allSessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getSessionDate() != null)
                .forEach(s -> doneSet.add(s.getSessionDate()));

        if (doneSet.isEmpty()) return new int[]{0, 0};

        List<LocalDate> sorted = new ArrayList<>(doneSet);
        Collections.sort(sorted);

        int longestStreak = 1, streak = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).minusDays(1).equals(sorted.get(i - 1))) {
                streak++;
                longestStreak = Math.max(longestStreak, streak);
            } else {
                streak = 1;
            }
        }

        int currentStreak = 0;
        LocalDate check = LocalDate.now();
        while (doneSet.contains(check) || doneSet.contains(check.minusDays(1))) {
            if (doneSet.contains(check)) {
                currentStreak++;
                check = check.minusDays(1);
            } else {
                break;
            }
        }

        return new int[]{currentStreak, longestStreak};
    }

    private Map<String, Integer> getWeeklyCalories(Long uid) {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String d : days) result.put(d, 0);

        sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, monday, monday.plusDays(6))
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getTotalCaloriesBurned() != null)
                .forEach(s -> {
                    int idx = s.getSessionDate().getDayOfWeek().getValue() - 1; // 0=Mon
                    result.put(days[idx], result.getOrDefault(days[idx], 0) + s.getTotalCaloriesBurned());
                });
        return result;
    }

    private Map<String, Integer> getWeeklyWorkouts(Long uid) {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Lấy giáo án đang active để đánh số "Tuần N" kể từ ngày tạo giáo án,
        // thay vì hiển thị tuần lịch tuyệt đối (W28, W29...).
        LocalDate planStartMonday = planRepository.findByUserIdAndIsActiveTrue(uid)
                .map(p -> p.getCreatedAt().toLocalDate().with(java.time.DayOfWeek.MONDAY))
                .orElse(null);

        for (int i = 3; i >= 0; i--) {
            LocalDate start = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
            LocalDate end   = start.plusDays(6);

            String label;
            if (planStartMonday != null) {
                long weekIndex = java.time.temporal.ChronoUnit.WEEKS.between(planStartMonday, start) + 1;
                label = weekIndex >= 1 ? "Tuần " + weekIndex : "—"; // trước khi có giáo án
            } else {
                label = "W" + start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            }

            long count = sessionRepository
                    .findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, start, end)
                    .stream().filter(s -> s.getStatus() == SessionStatus.SCHEDULED
                            || s.getStatus() == SessionStatus.COMPLETED
                            || s.getStatus() == SessionStatus.CHECKED_IN)
                    .count();
            result.put(label, (int) count);
        }
        return result;
    }

    private Map<String, Integer> getDailyDuration(Long uid) {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String d : days) result.put(d, 0);

        sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, monday, monday.plusDays(6))
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getDurationMinutes() != null)
                .forEach(s -> {
                    int idx = s.getSessionDate().getDayOfWeek().getValue() - 1;
                    result.put(days[idx], result.getOrDefault(days[idx], 0) + s.getDurationMinutes());
                });
        return result;
    }

    private Map<String, Integer> getDailyVolumePercent(Long uid) {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        Map<String, List<Integer>> buckets = new LinkedHashMap<>();
        for (String d : days) buckets.put(d, new ArrayList<>());

        sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, monday, monday.plusDays(6))
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getCompletionRate() != null)
                .forEach(s -> {
                    int idx = s.getSessionDate().getDayOfWeek().getValue() - 1;
                    buckets.get(days[idx]).add(s.getCompletionRate());
                });

        Map<String, Integer> result = new LinkedHashMap<>();
        for (String d : days) {
            List<Integer> vals = buckets.get(d);
            result.put(d, vals.isEmpty() ? 0 : (int) Math.round(vals.stream().mapToInt(Integer::intValue).average().orElse(0)));
        }
        return result;
    }

    private Map<String, Integer> getWeeklyDuration(Long uid) {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 3; i >= 0; i--) {
            LocalDate start = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
            LocalDate end   = start.plusDays(6);
            String label    = "W" + start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int sum = sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, start, end)
                    .stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getDurationMinutes() != null)
                    .mapToInt(WorkoutSession::getDurationMinutes).sum();
            result.put(label, sum);
        }
        return result;
    }

    private Map<String, Integer> getWeeklyVolumePercent(Long uid) {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 3; i >= 0; i--) {
            LocalDate start = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
            LocalDate end   = start.plusDays(6);
            String label    = "W" + start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            List<Integer> vals = sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, start, end)
                    .stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getCompletionRate() != null)
                    .map(WorkoutSession::getCompletionRate).collect(Collectors.toList());
            result.put(label, vals.isEmpty() ? 0 : (int) Math.round(vals.stream().mapToInt(Integer::intValue).average().orElse(0)));
        }
        return result;
    }

    /** Số phút "yêu cầu" của 1 buổi = tổng thời gian các bài tập ĐÃ ĐƯỢC GIÁO ÁN GÁN cho ngày đó.
     *  Lấy thẳng từ WorkoutPlanExercise (sets/duration/rest do AI hoặc Admin soạn giáo án ấn định),
     *  KHÔNG suy đoán — buổi nào thiếu dữ liệu (VD giáo án cũ) thì bỏ qua bài đó, không cộng bừa. */
    private int estimateTargetMinutes(WorkoutSession s) {
        if (s.getPlanDay() == null || s.getPlanDay().getExercises() == null) return 0;
        int totalSeconds = 0;
        for (WorkoutPlanExercise pe : s.getPlanDay().getExercises()) {
            if (pe.getSets() == null) continue;
            int workSec = pe.getDurationSeconds() != null ? pe.getDurationSeconds() : 0;
            int restSec = pe.getRestSeconds() != null ? pe.getRestSeconds() : 0;
            totalSeconds += pe.getSets() * (workSec + restSec);
        }
        return Math.round(totalSeconds / 60f);
    }

    private Map<String, Integer> getDailyDurationTarget(Long uid) {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String d : days) result.put(d, 0);

        sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, monday, monday.plusDays(6))
                .forEach(s -> {
                    int idx = s.getSessionDate().getDayOfWeek().getValue() - 1;
                    result.put(days[idx], result.getOrDefault(days[idx], 0) + estimateTargetMinutes(s));
                });
        return result;
    }

    private Map<String, Integer> getWeeklyDurationTarget(Long uid) {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 3; i >= 0; i--) {
            LocalDate start = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
            LocalDate end   = start.plusDays(6);
            String label    = "W" + start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int sum = sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, start, end)
                    .stream().mapToInt(this::estimateTargetMinutes).sum();
            result.put(label, sum);
        }
        return result;
    }

    /** Mục tiêu khối lượng luôn là 100% cho ngày/tuần có buổi tập được lên lịch, 0% nếu không có buổi nào. */
    private Map<String, Integer> getDailyVolumeTarget(Long uid) {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String d : days) result.put(d, 0);

        sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, monday, monday.plusDays(6))
                .forEach(s -> {
                    int idx = s.getSessionDate().getDayOfWeek().getValue() - 1;
                    result.put(days[idx], 100);
                });
        return result;
    }

    private Map<String, Integer> getWeeklyVolumeTarget(Long uid) {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 3; i >= 0; i--) {
            LocalDate start = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
            LocalDate end   = start.plusDays(6);
            String label    = "W" + start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            boolean hasAny = !sessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDate(uid, start, end).isEmpty();
            result.put(label, hasAny ? 100 : 0);
        }
        return result;
    }
}
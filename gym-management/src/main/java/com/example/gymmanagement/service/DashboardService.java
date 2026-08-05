package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.response.DashboardResponse;
import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.SessionStatus;
import com.example.gymmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
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
        MonthlyAnalytics analytics = buildMonthlyAnalytics(allSessions, progList);

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
                .currentMonthSessions(analytics.currentSessions)
                .previousMonthSessions(analytics.previousSessions)
                .currentMonthCompleted(analytics.currentCompleted)
                .currentMonthCalories(analytics.currentCalories)
                .currentMonthDurationMinutes(analytics.currentDuration)
                .currentMonthAdherencePercent(analytics.currentAdherence)
                .previousMonthAdherencePercent(analytics.previousAdherence)
                .sessionChangePercent(analytics.sessionChange)
                .adherenceChangePercent(analytics.adherenceChange)
                .currentMonthWeightChange(analytics.weightChange)
                .monthlyCompletedSessions(analytics.monthlySessions)
                .monthlyCaloriesBurned(analytics.monthlyCalories)
                .monthlyDurationMinutes(analytics.monthlyDuration)
                .monthlyCompletionPercent(analytics.monthlyCompletion)
                .muscleGroupDistribution(analytics.muscleGroups)
                .sessionQualityDistribution(analytics.quality)
                .monthlyInsight(analytics.insight)
                .recommendations(analytics.recommendations)
                .build();
    }

    private MonthlyAnalytics buildMonthlyAnalytics(List<WorkoutSession> sessions, List<ProgressTracking> progress) {
        LocalDate today = LocalDate.now();
        LocalDate currentStart = today.withDayOfMonth(1);
        LocalDate currentEnd = currentStart.plusMonths(1).minusDays(1);
        LocalDate previousStart = currentStart.minusMonths(1);
        LocalDate previousEnd = currentStart.minusDays(1);

        List<WorkoutSession> current = sessions.stream()
                .filter(s -> isBetween(s.getSessionDate(), currentStart, currentEnd)).collect(Collectors.toList());
        List<WorkoutSession> previous = sessions.stream()
                .filter(s -> isBetween(s.getSessionDate(), previousStart, previousEnd)).collect(Collectors.toList());

        int currentCompleted = (int) current.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
        int previousCompleted = (int) previous.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED).count();
        int currentAdherence = adherence(current, today);
        int previousAdherence = adherence(previous, previousEnd);

        Map<String, Integer> monthlySessions = new LinkedHashMap<>();
        Map<String, Integer> monthlyCalories = new LinkedHashMap<>();
        Map<String, Integer> monthlyDuration = new LinkedHashMap<>();
        Map<String, Integer> monthlyCompletion = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate start = currentStart.minusMonths(i);
            LocalDate end = start.plusMonths(1).minusDays(1);
            List<WorkoutSession> bucket = sessions.stream()
                    .filter(s -> isBetween(s.getSessionDate(), start, end)).collect(Collectors.toList());
            List<WorkoutSession> completed = bucket.stream()
                    .filter(s -> s.getStatus() == SessionStatus.COMPLETED).collect(Collectors.toList());
            String label = String.format("%02d/%d", start.getMonthValue(), start.getYear());
            monthlySessions.put(label, completed.size());
            monthlyCalories.put(label, completed.stream().filter(s -> s.getTotalCaloriesBurned() != null)
                    .mapToInt(WorkoutSession::getTotalCaloriesBurned).sum());
            monthlyDuration.put(label, completed.stream().filter(s -> s.getDurationMinutes() != null)
                    .mapToInt(WorkoutSession::getDurationMinutes).sum());
            monthlyCompletion.put(label, completed.isEmpty() ? null : (int) Math.round(completed.stream()
                    .filter(s -> s.getCompletionRate() != null).mapToInt(WorkoutSession::getCompletionRate)
                    .average().orElse(0)));
        }

        Map<String, Integer> muscleGroups = new LinkedHashMap<>();
        current.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .flatMap(s -> s.getExerciseLogs() == null ? java.util.stream.Stream.empty() : s.getExerciseLogs().stream())
                .filter(log -> Boolean.TRUE.equals(log.getIsCompleted()) && log.getExercise() != null
                        && log.getExercise().getMuscleGroup() != null)
                .forEach(log -> muscleGroups.merge(log.getExercise().getMuscleGroup().name(), 1, Integer::sum));

        Map<String, Integer> quality = new LinkedHashMap<>();
        quality.put("Hoàn thành tốt", 0); quality.put("Hoàn thành một phần", 0);
        quality.put("Bỏ buổi", 0); quality.put("Chưa hoàn thành", 0);
        current.stream().filter(s -> s.getSessionDate() != null && !s.getSessionDate().isAfter(today)).forEach(s -> {
            if (s.getStatus() == SessionStatus.COMPLETED && Optional.ofNullable(s.getCompletionRate()).orElse(0) >= 90)
                quality.merge("Hoàn thành tốt", 1, Integer::sum);
            else if (s.getStatus() == SessionStatus.COMPLETED) quality.merge("Hoàn thành một phần", 1, Integer::sum);
            else if (s.getStatus() == SessionStatus.SKIPPED) quality.merge("Bỏ buổi", 1, Integer::sum);
            else quality.merge("Chưa hoàn thành", 1, Integer::sum);
        });

        Double monthWeightChange = weightChangeForMonth(progress, currentStart, currentEnd);
        List<String> recommendations = new ArrayList<>();
        if (current.isEmpty()) recommendations.add("Hãy tạo hoặc bắt đầu giáo án để hệ thống có dữ liệu phân tích.");
        else if (currentAdherence < 70) recommendations.add("Ưu tiên duy trì lịch tập đều; mục tiêu gần nhất là đạt ít nhất 70% số buổi.");
        else recommendations.add("Nhịp tập tháng này ổn định; hãy tiếp tục giữ lịch tập hiện tại.");
        if (quality.get("Bỏ buổi") > 0) recommendations.add("Bạn có buổi đã bỏ; nên đổi ngày tập nếu lịch hiện tại khó duy trì.");
        if (muscleGroups.size() > 1) {
            String least = Collections.min(muscleGroups.entrySet(), Map.Entry.comparingByValue()).getKey();
            String most = Collections.max(muscleGroups.entrySet(), Map.Entry.comparingByValue()).getKey();
            if (!least.equals(most) && muscleGroups.get(most) >= muscleGroups.get(least) * 2)
                recommendations.add("Nhóm " + vietnameseMuscle(least) + " đang được tập ít hơn; cân nhắc bổ sung để cân bằng giáo án.");
        }
        String insight = "Tháng này bạn đã hoàn thành " + currentCompleted + "/" + eligibleCount(current, today)
                + " buổi (" + currentAdherence + "%). "
                + (currentCompleted >= previousCompleted ? "Số buổi hoàn thành đang bằng hoặc cao hơn tháng trước."
                : "Số buổi hoàn thành đang thấp hơn tháng trước.");

        MonthlyAnalytics result = new MonthlyAnalytics();
        result.currentSessions = current.size(); result.previousSessions = previous.size();
        result.currentCompleted = currentCompleted;
        result.currentCalories = current.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getTotalCaloriesBurned() != null)
                .mapToInt(WorkoutSession::getTotalCaloriesBurned).sum();
        result.currentDuration = current.stream().filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getDurationMinutes() != null)
                .mapToLong(WorkoutSession::getDurationMinutes).sum();
        result.currentAdherence = currentAdherence; result.previousAdherence = previousAdherence;
        result.sessionChange = percentChange(currentCompleted, previousCompleted);
        result.adherenceChange = Math.round((currentAdherence - previousAdherence) * 10.0) / 10.0;
        result.weightChange = monthWeightChange; result.monthlySessions = monthlySessions;
        result.monthlyCalories = monthlyCalories; result.monthlyDuration = monthlyDuration;
        result.monthlyCompletion = monthlyCompletion; result.muscleGroups = muscleGroups;
        result.quality = quality; result.insight = insight; result.recommendations = recommendations;
        return result;
    }

    private boolean isBetween(LocalDate value, LocalDate start, LocalDate end) {
        return value != null && !value.isBefore(start) && !value.isAfter(end);
    }

    private int eligibleCount(List<WorkoutSession> sessions, LocalDate cutoff) {
        return (int) sessions.stream().filter(s -> s.getSessionDate() != null && !s.getSessionDate().isAfter(cutoff)).count();
    }

    private int adherence(List<WorkoutSession> sessions, LocalDate cutoff) {
        int eligible = eligibleCount(sessions, cutoff);
        if (eligible == 0) return 0;
        long completed = sessions.stream().filter(s -> s.getSessionDate() != null && !s.getSessionDate().isAfter(cutoff)
                && s.getStatus() == SessionStatus.COMPLETED).count();
        return (int) Math.round(completed * 100.0 / eligible);
    }

    private Double percentChange(int current, int previous) {
        if (previous == 0) return current == 0 ? 0.0 : 100.0;
        return Math.round((current - previous) * 1000.0 / previous) / 10.0;
    }

    private Double weightChangeForMonth(List<ProgressTracking> progress, LocalDate start, LocalDate end) {
        List<ProgressTracking> values = progress.stream()
                .filter(p -> isBetween(p.getRecordedDate(), start, end) && p.getWeight() != null)
                .sorted(Comparator.comparing(ProgressTracking::getRecordedDate)).collect(Collectors.toList());
        if (values.size() < 2) return null;
        return Math.round((values.get(values.size() - 1).getWeight() - values.get(0).getWeight()) * 10.0) / 10.0;
    }

    private String vietnameseMuscle(String muscle) {
        return switch (muscle) {
            case "CHEST" -> "ngực"; case "BACK" -> "lưng"; case "SHOULDERS" -> "vai";
            case "ARMS" -> "tay"; case "LEGS" -> "chân"; case "CORE" -> "cơ lõi";
            case "CARDIO" -> "tim mạch"; case "FULL_BODY" -> "toàn thân";
            default -> muscle.toLowerCase();
        };
    }

    private static class MonthlyAnalytics {
        int currentSessions, previousSessions, currentCompleted, currentCalories, currentAdherence, previousAdherence;
        long currentDuration; Double sessionChange, adherenceChange, weightChange;
        Map<String, Integer> monthlySessions, monthlyCalories, monthlyDuration, monthlyCompletion, muscleGroups, quality;
        String insight; List<String> recommendations;
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

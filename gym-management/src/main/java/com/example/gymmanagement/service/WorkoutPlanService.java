package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.request.TemplateDayRequest;
import com.example.gymmanagement.dto.request.TemplateExerciseRequest;
import com.example.gymmanagement.dto.request.WorkoutPlanRequest;
import com.example.gymmanagement.dto.request.WorkoutTemplateRequest;
import com.example.gymmanagement.dto.response.*;
import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.*;
import com.example.gymmanagement.repository.*;
import com.example.gymmanagement.service.plan.MuscleGroupSplitPlanner;
import com.example.gymmanagement.service.schedule.ScheduleCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final WorkoutPlanRepository     planRepo;
    private final WorkoutPlanDayRepository  dayRepo;
    private final UserRepository            userRepo;
    private final ExerciseRepository        exerciseRepo;
    private final UserProfileRepository     profileRepo;
    private final WorkoutSessionRepository  sessionRepo;
    private final SessionExerciseLogRepository logRepo;
    private final FitnessCalculator fitnessCalculator;
    private final WorkoutPlanExerciseRepository planExerciseRepo;
    private final MembershipRepository membershipRepo;
    private final MembershipService membershipService;
    // ── MỚI (Patch 7) ──
    private final EnduranceTestRepository enduranceTestRepo;
    private final EstimatedWeeksCalculator estimatedWeeksCalculator;
    private final ManaService manaService;
    private final SystemConfigService systemConfigService;   // MỚI
    private final InjuryAreaOptionRepository injuryAreaOptionRepo;
    private final TrainingConfigService trainingConfigService;
    private static final int FREE_PLAN_LIMIT_PER_MONTH = 1;

    private void checkPlanGenerationLimit(User user) {
        boolean isVip = membershipRepo.findByUserIdAndIsActiveTrue(user.getId())
                .map(m -> m.getMembershipType() == MembershipType.VIP)
                .orElse(false);
        if (isVip) return;

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        long countThisMonth = planRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsTemplate()))
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().toLocalDate().isBefore(monthStart))
                .count();

        int freeLimit = (int) systemConfigService.get("FREE_PLAN_LIMIT_PER_MONTH", 1.0);
        if (countThisMonth >= freeLimit) {
            throw new RuntimeException("Gói Free chỉ được tạo/đổi giáo án " + FREE_PLAN_LIMIT_PER_MONTH +
                    " lần/tháng. Nâng cấp lên gói VIP để tạo giáo án không giới hạn.");
        }
    }

    // ─────────────────────────────────────────────────────────
    // 1. generateAIPlanWithGoal — FS + BodyType + Level + Goal + Mana + Target (Patch 7)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public WorkoutPlanResponse generateAIPlanWithGoal(String email, Goal goal,
                                                      FitnessLevel levelParam, Integer daysPerWeek,
                                                      Double targetDeltaKg,
                                                      String enduranceMetric,
                                                      Double enduranceTargetValue) {
        User user = getUser(email);
        UserProfile profile = profileRepo.findByUserId(user.getId()).orElse(null);
        LowCompletionContext lowCompletion = findLowCompletionContext(user);

        FitnessLevel level = levelParam != null ? levelParam
                : (profile != null && profile.getFitnessLevel() != null
                   ? profile.getFitnessLevel() : FitnessLevel.BEGINNER);
        level = adjustLevelByProfile(level, profile, goal);

        // Nếu gói thường đã có 2 tuần liên tiếp dưới 40%, dùng cả hồ sơ vừa cập nhật
        // và lịch sử đó để hạ tải giáo án mới. Đây là gợi ý, không chặn việc tập.
        if (!membershipService.isVip(user) && lowCompletion.triggered) {
            level = lowerLevel(level);
        }

        daysPerWeek = calcSessionsPerWeek(goal, daysPerWeek, profile);

        Double startBmi    = profile != null ? profile.getBmi()    : null;
        Double startWeight = profile != null ? profile.getWeight() : null;

        double fs = (profile != null && profile.getAge() != null
                && profile.getHeight() != null && profile.getWeight() != null)
                ? fitnessCalculator.calculateFS(profile.getAge(), profile.getHeight(),
                profile.getWeight(), profile.getGender())
                : 60.0;

        FitnessCalculator.FsLevel fsLevel = fitnessCalculator.getFsLevel(fs);

        FitnessCalculator.BodyType bodyType = (profile != null)
                ? fitnessCalculator.classifyBodyType(profile.getHeight(), profile.getWeight(), startBmi,
                profile.getGender(), profile.getBodyFatPercentage())
                : FitnessCalculator.BodyType.CAN_DOI;

        // ── MỚI (Patch 7): xác định target theo Goal — Business Rules v2 (LOCKED).
        // Validate TRƯỚC deactivateAndCleanOldPlan() để tránh huỷ giáo án cũ nếu request lỗi. ──
        AssessmentMetricType targetMetricTypeVal = null;
        Double targetBaselineValueVal = null;
        Double targetGoalValueVal = null;
        Double targetCurrentValueVal = null;

        switch (goal) {
            case MUSCLE_GAIN -> {
                if (targetDeltaKg == null || targetDeltaKg <= 0)
                    throw new RuntimeException("Mục tiêu Tăng cơ yêu cầu targetDeltaKg > 0");
                if (startWeight == null)
                    throw new RuntimeException("Cần có cân nặng trong hồ sơ để tạo giáo án theo mục tiêu này");
                targetBaselineValueVal = startWeight;
                targetGoalValueVal = startWeight + targetDeltaKg;
                targetCurrentValueVal = startWeight;
            }
            case WEIGHT_LOSS -> {
                if (targetDeltaKg == null || targetDeltaKg >= 0)
                    throw new RuntimeException("Mục tiêu Giảm cân yêu cầu targetDeltaKg < 0");
                if (startWeight == null)
                    throw new RuntimeException("Cần có cân nặng trong hồ sơ để tạo giáo án theo mục tiêu này");
                targetBaselineValueVal = startWeight;
                targetGoalValueVal = startWeight + targetDeltaKg;
                targetCurrentValueVal = startWeight;
            }
            case ENDURANCE -> {
                if (enduranceMetric == null || enduranceTargetValue == null)
                    throw new RuntimeException("Mục tiêu Sức bền yêu cầu enduranceMetric và enduranceTargetValue");

                AssessmentMetricType metricType;
                try {
                    metricType = AssessmentMetricType.valueOf(enduranceMetric);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("enduranceMetric không hợp lệ: " + enduranceMetric);
                }

                EnduranceTest test = enduranceTestRepo.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Bạn cần thực hiện bài test sức bền trước khi tạo giáo án cho mục tiêu này"));

                Double baseline = switch (metricType) {
                    case PUSHUP_REPS -> test.getPushupReps() != null ? test.getPushupReps().doubleValue() : null;
                    case PLANK_SECONDS -> test.getPlankSeconds() != null ? test.getPlankSeconds().doubleValue() : null;
                    case SQUAT_REPS -> test.getSquatReps() != null ? test.getSquatReps().doubleValue() : null;
                };
                if (baseline == null)
                    throw new RuntimeException("Kết quả bài test cho " + enduranceMetric + " chưa có dữ liệu");

                targetMetricTypeVal = metricType;      // đổi String -> AssessmentMetricType
                targetBaselineValueVal = baseline;
                targetGoalValueVal = enduranceTargetValue;
                targetCurrentValueVal = null;
            }
            case MAINTENANCE -> {
                // Không có targetGoalValue (LOCKED) — chỉ cần baseline để Patch 9 so sánh độ lệch.
                targetBaselineValueVal = startWeight;
                targetGoalValueVal = null;
                targetCurrentValueVal = startWeight;
            }
        }

        // ── MỚI (Patch 7): estimatedWeeks — tính qua module độc lập, KHÔNG hardcode ──
        int estimatedWeeks = estimatedWeeksCalculator.calculate(
                goal, level, targetDeltaKg,
                goal == Goal.ENDURANCE ? targetBaselineValueVal : null,
                goal == Goal.ENDURANCE ? targetGoalValueVal : null);

        deactivateAndCleanOldPlan(user.getId());

        double manaMultiplier = systemConfigService.get("MANA_MAX_MULTIPLIER", 2.0);
        int maxMana = Math.max(1, (int) Math.round(fs * manaMultiplier));

        WorkoutPlan plan = WorkoutPlan.builder()
                .user(user)
                .planName(buildPlanName(goal, level))
                .description(buildPlanDesc(goal, level, daysPerWeek, profile))
                .goal(goal).targetLevel(level)
                .durationWeeks(estimatedWeeks)
                .estimatedWeeks(estimatedWeeks)
                .sessionsPerWeek(daysPerWeek).currentWeek(1)
                .startingBmi(startBmi).startingWeight(startWeight)
                .isActive(true).isAiGenerated(true)
                .maxMana(maxMana)
                .currentMana(maxMana)
                .fitnessScore((int) Math.round(fs))
                .fitnessLevel(fsLevel)
                .bodyType(bodyType)
                .targetMetricType(targetMetricTypeVal)
                .targetBaselineValue(targetBaselineValueVal)
                .targetGoalValue(targetGoalValueVal)
                .targetCurrentValue(targetCurrentValueVal)
                .targetAchieved(false)
                .build();
        planRepo.save(plan);

        List<WorkoutPlanDay> days = buildPlanDaysNew(plan, goal, level, fsLevel, bodyType, daysPerWeek, profile, fs);
        if (!membershipService.isVip(user) && lowCompletion.triggered) {
            reducePlanLoad(days);
            plan.setDifficultyAdjustment(-1);
            plan.setSetsAdjustment(-1);
            plan.setRepsAdjustment(-2);
            plan.setWeightAdjustmentNote("Đã giảm tải theo tỷ lệ hoàn thành tuần "
                    + lowCompletion.week1 + " (" + lowCompletion.rate1 + "%) và tuần "
                    + lowCompletion.week2 + " (" + lowCompletion.rate2 + "%).");
        }
        dayRepo.saveAll(days);
        plan.setPlanDays(days);

        plan.setRequiredMaxSessionManaCost(computeMaxSessionManaCost(days));
        planRepo.save(plan);

        return toPlanResponse(plan, profile);
    }

    // ─────────────────────────────────────────────────────────
    // 2. Lấy giáo án
    // ─────────────────────────────────────────────────────────
    public WorkoutPlanResponse getActivePlan(String email) {
        User user = getUser(email);
        WorkoutPlan plan = planRepo.findByUserIdAndIsActiveTrue(user.getId()).orElse(null);
        if (plan == null) return null;
        plan.setPlanDays(dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(plan.getId()));
        UserProfile profile = profileRepo.findByUserId(user.getId()).orElse(null);
        return toPlanResponse(plan, profile);
    }

    public List<WorkoutPlanResponse> getAllPlans(String email) {
        User user = getUser(email);
        UserProfile profile = profileRepo.findByUserId(user.getId()).orElse(null);
        return planRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(p -> {
            p.setPlanDays(dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(p.getId()));
            return toPlanResponse(p, profile);
        }).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // 6. adjustPlanAfterWeek — giữ nguyên (Patch 8 sẽ sửa)
    // ─────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────
    // 6. adjustPlanAfterWeek — Patch 8: Business Rules v2 (chỉ áp dụng khi estimatedWeeks != null)
    // ─────────────────────────────────────────────────────────
    private static final double PROGRESS_TOLERANCE_PERCENT = 5.0;
    private static final int MIN_DURATION_WEEKS = 1;
    private static final int MAX_DURATION_WEEKS = 50;
    private static final String MAX_DURATION_END_MESSAGE =
            "Bạn đã hoàn thành thời lượng tối đa của giáo án nhưng chưa đạt mục tiêu. " +
                    "Hãy cân nhắc đánh giá lại tình trạng sức khỏe và tham khảo huấn luyện viên hoặc chuyên gia " +
                    "nếu cần trước khi tiếp tục với một giáo án mới.";

    @Transactional
    public WorkoutPlanResponse adjustPlanAfterWeek(Long planId, String email,
                                                   Double newWeight, Double newBodyFat) {
        User user = getUser(email);
        if (!membershipService.isVip(user)) {
            throw new RuntimeException("Tự động điều chỉnh giáo án theo tuần là quyền VIP. Vui lòng nâng cấp để sử dụng.");
        }
        WorkoutPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        int week = plan.getCurrentWeek() != null ? plan.getCurrentWeek() : 1;

        if (newWeight != null) {
            plan.setStartingWeight(newWeight);
            profileRepo.findByUserId(user.getId()).ifPresent(profile -> {
                profile.setWeight(newWeight);
                if (profile.getHeight() != null && profile.getHeight() > 0) {
                    double hM  = profile.getHeight() / 100.0;
                    double bmi = Math.round(newWeight / (hM * hM) * 10.0) / 10.0;
                    profile.setBmi(bmi);
                    plan.setStartingBmi(bmi);
                }
                if (newBodyFat != null) profile.setBodyFatPercentage(newBodyFat);
                profileRepo.save(profile);
            });
        }

        String note = null;
        boolean endPlanNow = false;


        if (plan.getEstimatedWeeks() == null) {
            // ── Giáo án cũ (tạo trước Patch 7) — chạy nguyên logic cũ (LOCKED) ──
            long completed = sessionRepo.countCompletedInWeek(user.getId(), planId, week);
            int  target    = plan.getSessionsPerWeek();
            if (completed < target) {
                plan.setDurationWeeks(plan.getDurationWeeks() + 1);
                note = "📅 Bạn bỏ " + (target - completed) + " buổi tuần này. Đã gia hạn thêm 1 tuần.";
            }
        } else {
            // ── Business Rules v2 ──
            Double current = resolveCurrentTargetValue(plan, newWeight);

            // ENDURANCE không ghi targetCurrentValue vào WorkoutPlan (LOCKED)
            if (plan.getGoal() != Goal.ENDURANCE && current != null) {
                plan.setTargetCurrentValue(current);
            }

            if (plan.getGoal() == Goal.MAINTENANCE) {
                // ── Chỉ chuẩn bị dữ liệu cho Patch 9 — không đánh giá đạt/chưa đạt,
                // không điều chỉnh durationWeeks (không có targetGoalValue để so sánh) ──
            } else if (plan.getTargetBaselineValue() != null && plan.getTargetGoalValue() != null && current != null) {
                double baseline = plan.getTargetBaselineValue();
                double goalVal = plan.getTargetGoalValue();
                Boolean achieved = checkAchieved(plan.getGoal(), baseline, goalVal, current);

                if (Boolean.TRUE.equals(achieved)) {
                    plan.setTargetAchieved(true);
                    plan.setIsCompleted(true);
                    plan.setIsActive(false);
                    endPlanNow = true;
                    note = "🎉 Chúc mừng! Bạn đã đạt mục tiêu của giáo án.";
                } else {
                    Double percentGoal = computePercentGoal(plan.getGoal(), baseline, goalVal, current);
                    if (percentGoal != null) {
                        double percentTime = (double) week / plan.getEstimatedWeeks() * 100;
                        double gap = percentGoal - percentTime;

                        int newDuration = plan.getDurationWeeks();
                        double tolerance = systemConfigService.get("PROGRESS_TOLERANCE_PERCENT", 5.0);
                        int minWeeks = (int) systemConfigService.get("MIN_DURATION_WEEKS", 1.0);
                        int maxWeeks = (int) systemConfigService.get("MAX_DURATION_WEEKS", 50.0);

                        if (gap > tolerance) {
                            newDuration -= 1;
                            note = "🚀 Tiến độ nhanh hơn dự kiến. Đã rút ngắn 1 tuần.";
                        } else if (gap < -tolerance) {
                            newDuration += 1;
                            note = "🐢 Tiến độ chậm hơn dự kiến. Đã gia hạn thêm 1 tuần.";
                        }
                        newDuration = Math.max(minWeeks, Math.min(maxWeeks, newDuration));
                        plan.setDurationWeeks(newDuration);

                        if (newDuration >= maxWeeks) {
                            plan.setIsCompleted(true);
                            plan.setIsActive(false);
                            endPlanNow = true;
                            note = MAX_DURATION_END_MESSAGE;
                        }
                    }
                }
            }
        }

        plan.setCurrentWeek(week + 1);

        if (!endPlanNow && plan.getCurrentWeek() > plan.getDurationWeeks()) {
            plan.setIsCompleted(true);
            plan.setIsActive(false);
        }

        plan.setPlanDays(dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(planId));
        WorkoutPlan saved = planRepo.save(plan);

        WorkoutPlanResponse resp = toPlanResponse(saved,
                profileRepo.findByUserId(user.getId()).orElse(null));
        if (note != null) resp.setScheduleNote(note);
        return resp;
    }

    @Transactional
    public List<String> adjustRepeatedLowCompletionExercises(WorkoutPlan plan, User user) {
        if (!membershipService.isVip(user)) return List.of();
        double threshold = systemConfigService.get("LOW_COMPLETION_THRESHOLD", 40.0);
        int action = (int) Math.round(systemConfigService.get("LOW_COMPLETION_ACTION", 1.0));
        UserProfile profile = profileRepo.findByUserId(user.getId()).orElse(null);
        List<WorkoutPlanExercise> items = planExerciseRepo.findByPlanDay_WorkoutPlan_Id(plan.getId());
        List<String> changes = new ArrayList<>();

        for (WorkoutPlanExercise pe : items) {
            List<SessionExerciseLog> recent = logRepo.findRecentExerciseLogs(user.getId(), plan.getId(),
                    pe.getExercise().getId(), org.springframework.data.domain.PageRequest.of(0, 2));
            if (recent.size() < 2 || recent.get(0).getCompletionPercent() >= threshold
                    || recent.get(1).getCompletionPercent() >= threshold) continue;
            Long lastProcessed = pe.getLastLowAdjustmentLogId();
            if (lastProcessed != null && recent.get(1).getId() <= lastProcessed) continue;

            String oldName = pe.getExercise().getName();
            boolean adjusted = false;
            if (action == 2) {
                adjusted = reduceOneExerciseVolume(pe, plan);
                if (adjusted) {
                    changes.add(oldName + ": đã giảm sets/reps trong ngưỡng mục tiêu");
                } else {
                    Exercise replacement = findEasierReplacement(pe, items, profile);
                    if (replacement != null) {
                        pe.setExercise(replacement); adjusted = true;
                        changes.add(oldName + " → " + replacement.getName() + " (đã chạm sàn sets/reps)");
                    }
                }
            } else {
                Exercise replacement = findEasierReplacement(pe, items, profile);
                if (replacement != null) {
                    pe.setExercise(replacement); adjusted = true;
                    changes.add(oldName + " → " + replacement.getName());
                } else {
                    adjusted = reduceOneExerciseVolume(pe, plan);
                    if (adjusted) changes.add(oldName + ": không có bài dễ phù hợp, đã giảm trong ngưỡng mục tiêu");
                }
            }
            if (!adjusted) continue;
            pe.setLastLowAdjustmentLogId(recent.get(0).getId());
            planExerciseRepo.save(pe);
        }
        return changes;
    }

    private boolean reduceOneExerciseVolume(WorkoutPlanExercise pe, WorkoutPlan plan) {
        int setsDown = Math.max(0, (int) Math.round(systemConfigService.get("LOW_COMPLETION_SETS_REDUCTION", 1.0)));
        int repsDown = Math.max(0, (int) Math.round(systemConfigService.get("LOW_COMPLETION_REPS_REDUCTION", 2.0)));
        FitnessCalculator.FsLevel fsLevel = plan.getFitnessLevel() != null ? plan.getFitnessLevel()
                : switch (plan.getTargetLevel()) {
                    case BEGINNER -> FitnessCalculator.FsLevel.WEAK;
                    case INTERMEDIATE -> FitnessCalculator.FsLevel.AVERAGE;
                    case ADVANCED -> FitnessCalculator.FsLevel.GOOD;
                };
        FitnessCalculator.BodyType bodyType = plan.getBodyType() != null
                ? plan.getBodyType() : FitnessCalculator.BodyType.CAN_DOI;
        var baseline = fitnessCalculator.resolveFinalSetsReps(fsLevel, plan.getGoal(), bodyType);
        int setFloor = baseline.sets();
        int repFloor = com.example.gymmanagement.service.setrep.TrainingZone.of(plan.getGoal()).repFloor();
        boolean changed = false;
        if (pe.getSets() != null) {
            int next = Math.max(setFloor, pe.getSets() - setsDown);
            changed |= next < pe.getSets(); pe.setSets(next);
        }
        if (pe.getReps() != null) {
            int next = Math.max(repFloor, pe.getReps() - repsDown);
            changed |= next < pe.getReps(); pe.setReps(next);
        }
        return changed;
    }

    private Exercise findEasierReplacement(WorkoutPlanExercise pe, List<WorkoutPlanExercise> all, UserProfile profile) {
        Difficulty current = pe.getExercise().getDifficulty();
        Difficulty easier = current == Difficulty.HARD ? Difficulty.MEDIUM : current == Difficulty.MEDIUM ? Difficulty.EASY : null;
        if (easier == null) return null;
        Set<Long> used = all.stream().map(x -> x.getExercise().getId()).collect(Collectors.toSet());
        List<Exercise> candidates = exerciseRepo.findByMuscleGroupAndDifficultyAndIsActiveTrue(
                pe.getExercise().getMuscleGroup(), easier).stream()
                .filter(e -> !used.contains(e.getId())).filter(e -> isExerciseAllowed(e, profile)).toList();
        return candidates.isEmpty() ? null : candidates.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    @Transactional
    public WorkoutPlanResponse advancePlanWeekWithoutAdjustment(Long planId, String email) {
        User user = getUser(email);
        WorkoutPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        if (plan.getUser() != null && !plan.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied");
        int nextWeek = (plan.getCurrentWeek() != null ? plan.getCurrentWeek() : 1) + 1;
        plan.setCurrentWeek(nextWeek);
        if (plan.getDurationWeeks() != null && nextWeek > plan.getDurationWeeks()) {
            plan.setIsCompleted(true);
            plan.setIsActive(false);
        }
        plan.setPlanDays(dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(planId));
        return toPlanResponse(planRepo.save(plan), profileRepo.findByUserId(user.getId()).orElse(null));
    }

    /** Lấy giá trị "current" theo Goal — MUSCLE_GAIN/WEIGHT_LOSS/MAINTENANCE dùng newWeight
     *  (fallback về targetCurrentValue cũ nếu không nhập); ENDURANCE đọc LIVE từ EnduranceTest. */
    private Double resolveCurrentTargetValue(WorkoutPlan plan, Double newWeight) {
        return switch (plan.getGoal()) {
            case MUSCLE_GAIN, WEIGHT_LOSS, MAINTENANCE ->
                    newWeight != null ? newWeight : plan.getTargetCurrentValue();
            case ENDURANCE -> readLiveEnduranceValue(plan);
        };
    }

    private Double readLiveEnduranceValue(WorkoutPlan plan) {
        if (plan.getUser() == null || plan.getTargetMetricType() == null) return null;
        AssessmentMetricType metricType = plan.getTargetMetricType();
        return enduranceTestRepo.findByUserId(plan.getUser().getId())
                .map(test -> switch (metricType) {
                    case PUSHUP_REPS -> test.getPushupReps() != null ? test.getPushupReps().doubleValue() : null;
                    case PLANK_SECONDS -> test.getPlankSeconds() != null ? test.getPlankSeconds().doubleValue() : null;
                    case SQUAT_REPS -> test.getSquatReps() != null ? test.getSquatReps().doubleValue() : null;
                })
                .orElse(null);
    }

    /** Ngưỡng đạt mục tiêu — LOCKED cho toàn bộ Goal có targetGoalValue (MUSCLE_GAIN,
     *  WEIGHT_LOSS, ENDURANCE dùng chung công thức baseline + (goal-baseline)×95%,
     *  đạt khi current >= threshold). WEIGHT_LOSS đảo chiều. MAINTENANCE không áp dụng. */
    private Boolean checkAchieved(Goal goal, double baseline, double goalVal, double current) {
        double threshold = systemConfigService.get("ACHIEVEMENT_THRESHOLD", 0.95);
        return switch (goal) {
            case MUSCLE_GAIN, ENDURANCE -> current >= (baseline + (goalVal - baseline) * threshold);
            case WEIGHT_LOSS -> current <= (baseline - (baseline - goalVal) * threshold);
            default -> null;
        };
    }

    /** %mục tiêu đã hoàn thành — dùng để so với %thời gian đã dùng (currentWeek/estimatedWeeks). */
    private Double computePercentGoal(Goal goal, double baseline, double goalVal, double current) {
        return switch (goal) {
            case MUSCLE_GAIN, ENDURANCE ->
                    (goalVal - baseline) != 0 ? (current - baseline) / (goalVal - baseline) * 100 : null;
            case WEIGHT_LOSS ->
                    (baseline - goalVal) != 0 ? (baseline - current) / (baseline - goalVal) * 100 : null;
            default -> null;
        };
    }




    @Transactional
    public WorkoutPlanExerciseResponse setBaseWeight(Long planExerciseId, Double weight) {
        WorkoutPlanExercise pe = planExerciseRepo.findById(planExerciseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài tập trong giáo án"));
        if (pe.getBaseWeightKg() != null)
            throw new RuntimeException("Tạ khởi điểm đã được thiết lập, không thể sửa lại.");
        if (weight == null || weight < 0)
            throw new RuntimeException("Giá trị tạ không hợp lệ");

        pe.setBaseWeightKg(weight);
        pe.setCurrentWeightKg(weight);
        planExerciseRepo.save(pe);
        return buildExResponse(pe);
    }



    @Transactional
    public WorkoutPlanResponse createManualTemplate(WorkoutTemplateRequest req) {
        validateTemplateRequest(req);

        WorkoutPlan plan = WorkoutPlan.builder()
                .user(null)
                .planName(req.getPlanName())
                .description(req.getDescription())
                .goal(req.getGoal())
                .targetLevel(req.getTargetLevel())
                .durationWeeks(req.getDurationWeeks())
                .sessionsPerWeek(req.getDays().size())
                .currentWeek(1)
                .isActive(true)
                .isAiGenerated(false)
                .isTemplate(true)
                .isFitnessImprovement(Boolean.TRUE.equals(req.getIsFitnessImprovement()))
                .build();
        planRepo.save(plan);

        List<WorkoutPlanDay> days = buildDaysFromRequest(plan, req.getDays());
        dayRepo.saveAll(days);
        plan.setPlanDays(days);

        return toPlanResponse(plan, null);
    }

    @Transactional
    public WorkoutPlanResponse updateManualTemplate(Long templateId, WorkoutTemplateRequest req) {
        validateTemplateRequest(req);

        WorkoutPlan plan = planRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!Boolean.TRUE.equals(plan.getIsTemplate())) {
            throw new RuntimeException("Plan này không phải template");
        }

        plan.setPlanName(req.getPlanName());
        plan.setDescription(req.getDescription());
        plan.setGoal(req.getGoal());
        plan.setTargetLevel(req.getTargetLevel());
        plan.setDurationWeeks(req.getDurationWeeks());
        plan.setSessionsPerWeek(req.getDays().size());
        plan.setIsFitnessImprovement(Boolean.TRUE.equals(req.getIsFitnessImprovement())); // MỚI

        List<WorkoutPlanDay> oldDays = dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(plan.getId());
        if (oldDays != null && !oldDays.isEmpty()) {
            sessionRepo.deleteByPlanDayIds(oldDays.stream().map(WorkoutPlanDay::getId).collect(Collectors.toList()));
            dayRepo.deleteAll(oldDays);
        }

        List<WorkoutPlanDay> newDays = buildDaysFromRequest(plan, req.getDays());
        List<WorkoutPlanDay> saved = dayRepo.saveAll(newDays);
        plan.setPlanDays(saved);

        WorkoutPlan savedPlan = planRepo.save(plan);
        return toPlanResponse(savedPlan, null);
    }

    public List<WorkoutPlanResponse> getAllTemplates(boolean onlyActive) {
        List<WorkoutPlan> templates = onlyActive
                ? planRepo.findByIsTemplateTrueAndIsActiveTrueOrderByCreatedAtDesc()
                : planRepo.findByIsTemplateTrueOrderByCreatedAtDesc();
        return templates.stream().map(p -> {
            p.setPlanDays(dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(p.getId()));
            return toPlanResponse(p, null);
        }).collect(Collectors.toList());
    }

    public List<WorkoutPlanResponse> getFitnessImprovementTemplates(Integer sessionsPerWeek) {
        List<WorkoutPlan> templates = planRepo
                .findByIsTemplateTrueAndIsFitnessImprovementTrueAndIsActiveTrueAndSessionsPerWeekOrderByCreatedAtDesc(
                        sessionsPerWeek);
        return templates.stream().map(p -> {
            p.setPlanDays(dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(p.getId()));
            return toPlanResponse(p, null);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        WorkoutPlan plan = planRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!Boolean.TRUE.equals(plan.getIsTemplate())) {
            throw new RuntimeException("Plan này không phải template");
        }
        plan.setIsActive(false);
        planRepo.save(plan);
    }

    @Transactional
    public WorkoutPlanResponse startFitnessImprovementPlan(String email, Long templateId) {
        User user = getUser(email);

        WorkoutPlan originalPlan = planRepo.findByUserIdAndIsActiveTrue(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Bạn cần có giáo án đang có hiệu lực để chuyển sang giáo án nâng cao thể lực"));

// ── SỬA: mở rộng cho cả AI Plan và Admin Plan (đã cá nhân hóa qua selectTemplate()).
// Chỉ chặn Fitness Improvement Plan (không lồng FI trong FI) và Template gốc
// (chưa từng được cá nhân hóa cho user cụ thể). ──
        if (Boolean.TRUE.equals(originalPlan.getIsFitnessImprovement())
                || Boolean.TRUE.equals(originalPlan.getIsTemplate())) {
            throw new RuntimeException("Giáo án hiện tại không thể chuyển sang giáo án nâng cao thể lực");
        }

        WorkoutPlan template = planRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!Boolean.TRUE.equals(template.getIsTemplate())
                || !Boolean.TRUE.equals(template.getIsFitnessImprovement())) {
            throw new RuntimeException("Plan này không phải Template giáo án nâng cao thể lực");
        }

        // ── Tính bù snapshot cho AI Plan cũ (tạo trước khi có requiredMaxSessionManaCost) ──
        if (originalPlan.getRequiredMaxSessionManaCost() == null) {
            List<WorkoutPlanDay> originalDays = dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(originalPlan.getId());
            originalPlan.setRequiredMaxSessionManaCost(computeMaxSessionManaCost(originalDays));
            planRepo.save(originalPlan);
        }

        List<WorkoutPlanDay> templateDays = dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(template.getId());
        UserProfile profile = profileRepo.findByUserId(user.getId()).orElse(null);

        double fs = (profile != null && profile.getAge() != null
                && profile.getHeight() != null && profile.getWeight() != null)
                ? fitnessCalculator.calculateFS(profile.getAge(), profile.getHeight(),
                profile.getWeight(), profile.getGender())
                : 60.0;
        FitnessCalculator.FsLevel fsLevel = fitnessCalculator.getFsLevel(fs);
        FitnessCalculator.BodyType bodyType = (profile != null)
                ? fitnessCalculator.classifyBodyType(profile.getHeight(), profile.getWeight(),
                profile.getBmi(), profile.getGender(), profile.getBodyFatPercentage())
                : FitnessCalculator.BodyType.CAN_DOI;
        double manaMultiplier = systemConfigService.get("MANA_MAX_MULTIPLIER", 2.0);
        int maxMana = Math.max(1, (int) Math.round(fs * manaMultiplier));

        // ── Goal/Level LẤY TỪ AI PLAN GỐC — không lấy từ Template ──
        Goal originalGoal = originalPlan.getGoal();
        FitnessLevel originalLevel = originalPlan.getTargetLevel();

        pauseActivePlan(user.getId());

        WorkoutPlan fiPlan = WorkoutPlan.builder()
                .user(user)
                .planName(template.getPlanName())
                .description(template.getDescription())
                .goal(originalGoal)
                .targetLevel(originalLevel)
                .durationWeeks(template.getDurationWeeks())
                .sessionsPerWeek(template.getSessionsPerWeek())
                .currentWeek(1)
                .startingBmi(profile != null ? profile.getBmi() : null)
                .startingWeight(profile != null ? profile.getWeight() : null)
                .isActive(true)
                .isAiGenerated(false)
                .isTemplate(false)
                .isFitnessImprovement(true)
                .originalPlanId(originalPlan.getId())
                .fitnessScore((int) Math.round(fs))
                .fitnessLevel(fsLevel)
                .bodyType(bodyType)
                .maxMana(maxMana)
                .currentMana(maxMana)
                .build();
        planRepo.save(fiPlan);

        List<WorkoutPlanDay> copiedDays = new ArrayList<>();
        for (WorkoutPlanDay srcDay : templateDays) {
            WorkoutPlanDay newDay = WorkoutPlanDay.builder()
                    .workoutPlan(fiPlan)
                    .dayOfWeek(srcDay.getDayOfWeek())
                    .dayName(srcDay.getDayName())
                    .build();

            List<WorkoutPlanExercise> copiedExercises = new ArrayList<>();
            if (srcDay.getExercises() != null) {
                for (WorkoutPlanExercise srcEx : srcDay.getExercises()) {
                    copiedExercises.add(personalizeTemplateExercise(newDay, srcEx, originalGoal,
                            originalLevel, fsLevel, bodyType, fs, profile, true));
                }
            }
            newDay.setExercises(copiedExercises);
            copiedDays.add(newDay);
        }

        List<WorkoutPlanDay> savedDays = dayRepo.saveAll(copiedDays);
        fiPlan.setPlanDays(savedDays);

        return toPlanResponse(fiPlan, profile);
    }

    /** Gọi sau checkout buổi cuối tuần của FI Plan. Nếu đủ thể lực -> Resume AI Plan
     *  (chỉ đổi isActive + đồng bộ fitnessScore/fitnessLevel/maxMana, KHÔNG đụng
     *  currentWeek/weekStartDate/WorkoutSession). Nếu chưa đủ -> tăng currentWeek của FI Plan. */
    @Transactional
    public void checkFitnessImprovementProgress(WorkoutPlan fiPlan, String email) {
        User user = getUser(email);
        UserProfile profile = profileRepo.findByUserId(user.getId()).orElse(null);

        double fs = (profile != null && profile.getAge() != null
                && profile.getHeight() != null && profile.getWeight() != null)
                ? fitnessCalculator.calculateFS(profile.getAge(), profile.getHeight(),
                profile.getWeight(), profile.getGender())
                : 60.0;
        double manaMultiplier = systemConfigService.get("MANA_MAX_MULTIPLIER", 2.0);
        int newMaxMana = (int) Math.round(fs * manaMultiplier);

        WorkoutPlan originalPlan = planRepo.findById(fiPlan.getOriginalPlanId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy giáo án AI gốc, id=" + fiPlan.getOriginalPlanId()));

        int required = originalPlan.getRequiredMaxSessionManaCost() != null
                ? originalPlan.getRequiredMaxSessionManaCost() : 0;

        double enoughThreshold = systemConfigService.get("MANA_ENOUGH_THRESHOLD", 0.75);
        boolean enough = required <= newMaxMana * enoughThreshold;

        if (enough) {
            fiPlan.setIsCompleted(true);
            fiPlan.setIsActive(false);
            planRepo.save(fiPlan);

            // ── Resume AI Plan: đồng bộ snapshot thể lực mới nhất, KHÔNG đụng
            // currentWeek/weekStartDate/currentMana (currentMana để ManaService tự regen). ──
            FitnessCalculator.FsLevel fsLevel = fitnessCalculator.getFsLevel(fs);
            originalPlan.setFitnessScore((int) Math.round(fs));
            originalPlan.setFitnessLevel(fsLevel);
            originalPlan.setMaxMana(newMaxMana);
            originalPlan.setIsActive(true);
            planRepo.save(originalPlan);
        } else {
            int nextWeek = (fiPlan.getCurrentWeek() != null ? fiPlan.getCurrentWeek() : 1) + 1;
            fiPlan.setCurrentWeek(nextWeek);
            planRepo.save(fiPlan);
        }
    }

    @Transactional
    public WorkoutPlanResponse selectTemplate(String email, Long templateId) {
        User user = getUser(email);
        checkPlanGenerationLimit(user);
        WorkoutPlan template = planRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!Boolean.TRUE.equals(template.getIsTemplate())) {
            throw new RuntimeException("Plan này không phải template");
        }
        List<WorkoutPlanDay> templateDays = dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(template.getId());
        UserProfile profile = profileRepo.findByUserId(user.getId()).orElse(null);

        // ── MỚI: snapshot thể lực/thể trạng của User — TÁI SỬ DỤNG đúng công thức AI
        // (fitnessCalculator.calculateFS/getFsLevel/classifyBodyType), KHÔNG viết mới ──
        double fs = (profile != null && profile.getAge() != null
                && profile.getHeight() != null && profile.getWeight() != null)
                ? fitnessCalculator.calculateFS(profile.getAge(), profile.getHeight(),
                profile.getWeight(), profile.getGender())
                : 60.0;
        FitnessCalculator.FsLevel fsLevel = fitnessCalculator.getFsLevel(fs);
        FitnessCalculator.BodyType bodyType = (profile != null)
                ? fitnessCalculator.classifyBodyType(profile.getHeight(), profile.getWeight(),
                profile.getBmi(), profile.getGender(), profile.getBodyFatPercentage())
                : FitnessCalculator.BodyType.CAN_DOI;
        double manaMultiplier = systemConfigService.get("MANA_MAX_MULTIPLIER", 2.0);
        int maxMana = Math.max(1, (int) Math.round(fs * manaMultiplier));

        deactivateAndCleanOldPlan(user.getId());

        WorkoutPlan newPlan = WorkoutPlan.builder()
                .user(user)
                .planName(template.getPlanName())
                .description(template.getDescription())
                .goal(template.getGoal())
                .targetLevel(template.getTargetLevel())
                .durationWeeks(template.getDurationWeeks())
                .sessionsPerWeek(template.getSessionsPerWeek())
                .currentWeek(1)
                .startingBmi(profile != null ? profile.getBmi() : null)
                .startingWeight(profile != null ? profile.getWeight() : null)
                .isActive(true)
                .isAiGenerated(false)
                .isTemplate(false)
                // ── MỚI: snapshot dùng chung với AI — cần cho Set/Rep, Weight, Mana ──
                .fitnessScore((int) Math.round(fs))
                .fitnessLevel(fsLevel)
                .bodyType(bodyType)
                .maxMana(maxMana)
                .currentMana(maxMana)
                .build();
        planRepo.save(newPlan);

        List<WorkoutPlanDay> copiedDays = new ArrayList<>();
        for (WorkoutPlanDay srcDay : templateDays) {
            WorkoutPlanDay newDay = WorkoutPlanDay.builder()
                    .workoutPlan(newPlan)
                    .dayOfWeek(srcDay.getDayOfWeek())   // ── GIỮ NGUYÊN lịch Admin ──
                    .dayName(srcDay.getDayName())
                    .build();

            List<WorkoutPlanExercise> copiedExercises = new ArrayList<>();
            if (srcDay.getExercises() != null) {
                for (WorkoutPlanExercise srcEx : srcDay.getExercises()) {
                    copiedExercises.add(personalizeTemplateExercise(newDay, srcEx, template.getGoal(),
                            template.getTargetLevel(), fsLevel, bodyType, fs, profile, false));
                }
            }
            newDay.setExercises(copiedExercises);
            copiedDays.add(newDay);
        }

        List<WorkoutPlanDay> savedDays = dayRepo.saveAll(copiedDays);
        newPlan.setPlanDays(savedDays);

        // ── MỚI: tính RequiredMaxSessionManaCost SAU KHI đã cá nhân hóa (Sets/Reps/Duration/Weight),
// tái dùng đúng computeMaxSessionManaCost() đang dùng cho AI Plan — KHÔNG viết công thức mới ──
        newPlan.setRequiredMaxSessionManaCost(computeMaxSessionManaCost(savedDays));
        planRepo.save(newPlan);

        return toPlanResponse(newPlan, profile);
    }

    /** MỚI: cá nhân hoá 1 WorkoutPlanExercise copy từ Admin Template theo ĐÚNG các hàm
     *  cá nhân hoá đang dùng cho giáo án AI (resolveFinalSetsReps/adjustDuration/calcRest/
     *  computeRecommendedWeightKg) — KHÔNG viết công thức mới. GIỮ NGUYÊN exercise/orderIndex/
     *  notes do Admin chọn. Quyết định bài rep-based hay duration-based dựa trên
     *  srcEx.getReps() != null (tôn trọng lựa chọn Admin — đã xác nhận Q5). */
    private WorkoutPlanExercise personalizeTemplateExercise(WorkoutPlanDay newDay, WorkoutPlanExercise srcEx,
                                                            Goal goal, FitnessLevel level,
                                                            FitnessCalculator.FsLevel fsLevel,
                                                            FitnessCalculator.BodyType bodyType,
                                                            double fs, UserProfile profile,
                                                            boolean isFitnessImprovement) {
        var srResult = fitnessCalculator.resolveFinalSetsReps(fsLevel, goal, bodyType);
        int finalSets = srResult.sets();
        int finalReps = srResult.reps();

        Exercise ex = srcEx.getExercise();

        // ── FI Plan: điều kiện theo Exercise catalog (giống AI Plan).
        // Template thường: giữ nguyên điều kiện theo dữ liệu Admin cấu hình. ──
        Integer newReps;
        Integer newDuration;
        if (isFitnessImprovement) {
            newReps = ex.getDefaultReps() != null ? finalReps : null;
            newDuration = ex.getDefaultDurationSeconds() != null
                    ? adjustDuration(ex.getDefaultDurationSeconds(), level) : null;
        } else {
            newReps = srcEx.getReps() != null ? finalReps : null;
            newDuration = srcEx.getDurationSeconds() != null
                    ? adjustDuration(srcEx.getDurationSeconds(), level) : null;
        }

        Integer newRest = calcRest(srcEx.getRestSeconds(), goal);

        Double recommendedWeightKg = computeRecommendedWeightKg(
                ex.getMuscleGroup(), ex.getUsesWeight(), fs, bodyType, goal, profile);

        return WorkoutPlanExercise.builder()
                .planDay(newDay)
                .exercise(ex)
                .sets(finalSets)
                .reps(newReps)
                .durationSeconds(newDuration)
                .restSeconds(newRest)
                .orderIndex(srcEx.getOrderIndex())
                .notes(srcEx.getNotes())
                .recommendedWeightKg(recommendedWeightKg)
                .currentRecommendedWeightKg(recommendedWeightKg)
                .build();
    }

    private void validateTemplateRequest(WorkoutTemplateRequest req) {
        if (req.getDays() == null || req.getDays().isEmpty()) {
            throw new RuntimeException("Template cần ít nhất 1 ngày tập");
        }
        for (TemplateDayRequest d : req.getDays()) {
            if (d.getExercises() == null || d.getExercises().isEmpty()) {
                throw new RuntimeException("Mỗi ngày tập cần ít nhất 1 bài tập (ngày: " + d.getDayName() + ")");
            }
        }
    }

    private List<WorkoutPlanDay> buildDaysFromRequest(WorkoutPlan plan, List<TemplateDayRequest> dayReqs) {
        List<WorkoutPlanDay> days = new ArrayList<>();
        for (TemplateDayRequest dReq : dayReqs) {
            WorkoutPlanDay day = WorkoutPlanDay.builder()
                    .workoutPlan(plan)
                    .dayOfWeek(dReq.getDayOfWeek())
                    .dayName(dReq.getDayName())
                    .build();

            List<WorkoutPlanExercise> exercises = new ArrayList<>();
            int idx = 1;
            for (TemplateExerciseRequest exReq : dReq.getExercises()) {
                Exercise ex = exerciseRepo.findById(exReq.getExerciseId())
                        .orElseThrow(() -> new RuntimeException("Exercise id=" + exReq.getExerciseId() + " không tồn tại"));
                exercises.add(WorkoutPlanExercise.builder()
                        .planDay(day)
                        .exercise(ex)
                        .sets(exReq.getSets())
                        .reps(exReq.getReps())
                        .durationSeconds(exReq.getDurationSeconds())
                        .restSeconds(exReq.getRestSeconds())
                        .orderIndex(exReq.getOrderIndex() != null ? exReq.getOrderIndex() : idx)
                        .notes(exReq.getNotes())
                        .build());
                idx++;
            }
            day.setExercises(exercises);
            days.add(day);
        }
        return days;
    }

    private int calcSessionsPerWeek(Goal goal, Integer requested, UserProfile profile) {
        int fromProfile = (profile != null && profile.getAvailableDaysPerWeek() != null)
                ? profile.getAvailableDaysPerWeek() : 3;
        int val = requested != null ? requested : fromProfile;

        int minRequired = switch (goal) {
            case MUSCLE_GAIN, WEIGHT_LOSS -> 4;
            case MAINTENANCE -> 3;
            case ENDURANCE -> 2 ;
        };
        int maxRequired = switch (goal) {
            case MUSCLE_GAIN, WEIGHT_LOSS -> 6;
            case  MAINTENANCE -> 5;
            case ENDURANCE  -> 4 ;
        };

        // Lịch rảnh là ràng buộc cứng, không ép user tập nhiều hơn số ngày đã khai báo.
        int available = Math.max(1, Math.min(7, fromProfile));
        val = Math.min(val, available);
        return Math.max(1, Math.min(maxRequired, val));
    }

    private FitnessLevel adjustLevelByBmi(FitnessLevel level, Double bmi, Goal goal) {
        if (bmi == null) return level;
        if (goal == Goal.WEIGHT_LOSS && bmi > 30 && level == FitnessLevel.ADVANCED)
            return FitnessLevel.INTERMEDIATE;
        if (goal == Goal.WEIGHT_LOSS && bmi > 35)
            return FitnessLevel.BEGINNER;
        return level;
    }

    private FitnessLevel adjustLevelByProfile(FitnessLevel requested, UserProfile profile, Goal goal) {
        if (profile == null) return requested;
        FitnessLevel safe = adjustLevelByBmi(requested, profile.getBmi(), goal);
        Integer months = profile.getTrainingExperienceMonths();
        // Số tháng tính từ lần tập gần đây nhất: nghỉ trên 1 năm thì hạ một bậc.
        // BEGINNER đã là mức thấp nhất nên được giữ nguyên.
        if (months != null && months > 12) safe = lowerLevel(safe);
        return safe;
    }

    private List<WorkoutPlanDay> buildPlanDaysNew(WorkoutPlan plan, Goal goal,
                                                  FitnessLevel level,
                                                  FitnessCalculator.FsLevel fsLevel,
                                                  FitnessCalculator.BodyType bodyType,
                                                  int sessions,
                                                  UserProfile profile,
                                                  double fs) {
        List<Map<MuscleGroup, Integer>> weekPlan = MuscleGroupSplitPlanner.buildWeekPlan(
                goal, level, sessions, trainingConfigService.dayGroups(goal, sessions));
        List<Integer> defaultSchedule = resolveSchedule(profile, sessions);
        String[] names = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        List<WorkoutPlanDay> days = new ArrayList<>();
        for (int i = 0; i < sessions; i++) {
            int dow = defaultSchedule.get(i);
            WorkoutPlanDay day = WorkoutPlanDay.builder()
                    .workoutPlan(plan)
                    .dayOfWeek(dow)
                    .dayName(names[dow])
                    .build();

            List<WorkoutPlanExercise> exercises =
                    buildExercisesNew(day, weekPlan.get(i), goal, level, fsLevel, bodyType, profile, fs);

            // ── XOÁ (Patch 10, mục 5/10/14): không còn append Assessment Exercise.
            // Assessment giờ chỉ tồn tại trong Popup Review (CheckOutRequest), không
            // còn là WorkoutPlanExercise. buildAssessmentExercise() đã bị xoá khỏi class. ──

            day.setExercises(exercises);
            days.add(day);
        }
        return days;
    }

// ── XOÁ HOÀN TOÀN (Patch 10): buildAssessmentExercise() không còn được gọi ở đâu,
// đã xoá khỏi class. ExerciseRepository.findFirstByAssessmentMetricTypeAndIsActiveTrue
// vẫn còn tồn tại trong Repository nhưng không còn được dùng. ──

    /** Append Assessment Exercise SAU khi buildExercisesNew() đã chạy xong
     *  -> không đi qua MuscleGroupSplitPlanner -> không ảnh hưởng chia nhóm cơ,
     *  không ảnh hưởng số lượng bài tập thường. Dùng chung cho MỌI Goal có targetMetricType. */
    private WorkoutPlanExercise buildAssessmentExercise(WorkoutPlanDay day, AssessmentMetricType metricType, int orderIndex) {
        Exercise testEx = exerciseRepo.findFirstByAssessmentMetricTypeAndIsActiveTrue(metricType)
                .orElseThrow(() -> new IllegalStateException(
                        "Thiếu Assessment Exercise cho AssessmentMetricType=" + metricType
                                + " — cần seed Exercise tương ứng trước khi tạo giáo án. Đây là lỗi cấu hình dữ liệu."));

        return WorkoutPlanExercise.builder()
                .planDay(day)
                .exercise(testEx)
                .sets(1)
                .reps(testEx.getDefaultReps())
                .durationSeconds(testEx.getDefaultDurationSeconds())
                .restSeconds(0)
                .orderIndex(orderIndex)
                .notes("🎯 Bài kiểm tra tiến độ mục tiêu — cố gắng hết sức để hệ thống đánh giá chính xác")
                .isAssessment(true)
                .build();
    }

    private List<WorkoutPlanExercise> buildExercisesNew(WorkoutPlanDay day,
                                                        Map<MuscleGroup, Integer> groupCounts,
                                                        Goal goal,
                                                        FitnessLevel level,
                                                        FitnessCalculator.FsLevel fsLevel,
                                                        FitnessCalculator.BodyType bodyType,
                                                        UserProfile profile,
                                                        double fs) {
        var srResult = fitnessCalculator.resolveFinalSetsReps(fsLevel, goal, bodyType);
        int finalSets = srResult.sets();
        int finalReps = srResult.reps();

        List<WorkoutPlanExercise> result = new ArrayList<>();
        int idx = 1;

        for (Map.Entry<MuscleGroup, Integer> entry : groupCounts.entrySet()) {
            MuscleGroup mg = entry.getKey();
            int need = entry.getValue();
            List<Exercise> cands = getExercisesByLevelAndGoal(mg, goal, level, need, profile);
            for (Exercise ex : cands) {
                String note = buildNote(ex, goal);
                if (srResult.loadHint() != com.example.gymmanagement.service.setrep.SetRepModels.LoadHint.NONE) {
                    String hintText = srResult.loadHint()
                            == com.example.gymmanagement.service.setrep.SetRepModels.LoadHint.INCREASE_WEIGHT
                            ? "💡 Gợi ý: tăng tạ khi thấy nhẹ"
                            : "💡 Gợi ý: giảm tạ để giữ đúng kỹ thuật";
                    note = (note == null) ? hintText : note + " — " + hintText;
                }

                Double recommendedWeightKg = computeRecommendedWeightKg(
                        mg, ex.getUsesWeight(), fs, bodyType, goal, profile);

                result.add(WorkoutPlanExercise.builder()
                        .planDay(day).exercise(ex)
                        .sets(finalSets)
                        .reps(ex.getDefaultReps() != null ? finalReps : null)
                        .durationSeconds(ex.getDefaultDurationSeconds() != null
                                ? adjustDuration(ex.getDefaultDurationSeconds(), level) : null)
                        .restSeconds(calcRest(ex.getRestSeconds(), goal))
                        .orderIndex(idx++)
                        .notes(note)
                        .recommendedWeightKg(recommendedWeightKg)
                        // ── MỚI: Current Recommendation khởi tạo = giá trị gốc lúc tạo giáo án ──
                        .currentRecommendedWeightKg(recommendedWeightKg)
                        .build());
            }
        }
        int duration = profile != null && profile.getPreferredSessionDuration() != null
                ? profile.getPreferredSessionDuration() : 60;
        int maxExercises = Math.max(2, Math.min(10, duration / 10));
        return result.size() > maxExercises
                ? new ArrayList<>(result.subList(0, maxExercises)) : result;
    }

    private Double computeRecommendedWeightKg(MuscleGroup mg, Boolean usesWeight, double fs,
                                              FitnessCalculator.BodyType bodyType, Goal goal,
                                              UserProfile profile) {
        if (usesWeight == null || !usesWeight) return null;

        Double muscleFactor = muscleFactorFor(mg);
        if (muscleFactor == null) return null;

        if (profile == null || profile.getWeight() == null || profile.getHeight() == null) return null;

        double weight = profile.getWeight();
        double height = profile.getHeight();
        String gender = profile.getGender();

        double fsFactor = fsFactorFor(fs);
        double bodyTypeFactor = bodyTypeFactorFor(bodyType);
        double goalFactor = goalFactorFor(goal);

        double idealWeight = fitnessCalculator.wChuan(height, gender);
        double delta = weight - idealWeight;
        double deltaFactor = deltaFactorFor(delta);

        double raw = weight * muscleFactor * fsFactor * bodyTypeFactor * goalFactor * deltaFactor;
        return Math.round(raw * 2) / 2.0;
    }

    private Double muscleFactorFor(MuscleGroup mg) {
        return switch (mg) {
            case CHEST -> 0.40;
            case BACK -> 0.50;
            case LEGS -> 0.60;
            case SHOULDERS -> 0.25;
            case ARMS -> 0.20;
            case CORE -> 0.15;
            default -> null;
        };
    }

    private double fsFactorFor(double fs) {
        if (fs >= 90) return 1.20;
        if (fs >= 80) return 1.10;
        if (fs >= 60) return 1.00;
        if (fs >= 40) return 0.85;
        return 0.75;
    }

    private double bodyTypeFactorFor(FitnessCalculator.BodyType bt) {
        return switch (bt) {
            case CAO_GAY -> 0.85;
            case GAY_CAN_DOI -> 0.90;
            case CAN_DOI -> 1.00;
            case CO_BAP -> 1.10;
            case VAN_DONG_VIEN -> 1.20;
            case THUA_CAN -> 0.95;
        };
    }

    private double goalFactorFor(Goal goal) {
        return switch (goal) {
            case MUSCLE_GAIN -> 1.05;
            case WEIGHT_LOSS -> 0.90;
            case MAINTENANCE -> 1.00;
            case ENDURANCE -> 0.6;
        };
    }

    private double deltaFactorFor(double delta) {
        if (delta < -10) return 0.85;
        if (delta < -5) return 0.90;
        if (delta <= 5) return 1.00;
        if (delta <= 10) return 1.05;
        return 1.00;
    }

    private List<Exercise> getExercisesByLevelAndGoal(MuscleGroup mg, Goal goal,
                                                      FitnessLevel level, int need, UserProfile profile) {
        List<Exercise> result = new ArrayList<>();

        List<Difficulty> priorityOrder = switch (level) {
            case BEGINNER     -> List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
            case INTERMEDIATE -> List.of(Difficulty.MEDIUM, Difficulty.EASY, Difficulty.HARD);
            case ADVANCED     -> List.of(Difficulty.HARD, Difficulty.MEDIUM, Difficulty.EASY);
        };

        for (Difficulty diff : priorityOrder) {
            if (result.size() >= need) break;
            List<Exercise> pool = exerciseRepo
                    .findByMuscleGroupAndDifficultyAndIsActiveTrue(mg, diff);
            pool = pool.stream().filter(ex -> isExerciseAllowed(ex, profile)).collect(Collectors.toList());
            pool.sort((a, b) -> {
                int cmp = Integer.compare(getGoalScore(b, goal), getGoalScore(a, goal));
                return cmp != 0 ? cmp : Long.compare(a.getId(), b.getId());
            });
            for (Exercise ex : pool) {
                if (result.size() >= need) break;
                result.add(ex);
            }
        }
        return result;
    }

    private List<Integer> resolveSchedule(UserProfile profile, int sessions) {
        if (profile != null && profile.getPreferredTrainingDays() != null
                && !profile.getPreferredTrainingDays().isBlank()) {
            List<Integer> selected = Arrays.stream(profile.getPreferredTrainingDays().split(","))
                    .map(String::trim).filter(s -> s.matches("[1-7]"))
                    .map(Integer::valueOf).distinct().sorted().collect(Collectors.toList());
            if (selected.size() >= sessions) return new ArrayList<>(selected.subList(0, sessions));
        }
        return trainingConfigService.recommendedDays(sessions);
    }

    /** Lọc cứng trước khi chấm điểm: thiếu thiết bị, chấn thương hoặc user từ chối thì loại. */
    private boolean isExerciseAllowed(Exercise ex, UserProfile profile) {
        if (profile == null) return true;
        String name = Optional.ofNullable(ex.getName()).orElse("").toLowerCase(Locale.ROOT);

        if (profile.getDislikedExercises() != null && !profile.getDislikedExercises().isBlank()) {
            boolean disliked = Arrays.stream(profile.getDislikedExercises().toLowerCase(Locale.ROOT).split(","))
                    .map(String::trim).filter(s -> !s.isBlank()).anyMatch(name::contains);
            if (disliked) return false;
        }

        Set<String> injuries = new HashSet<>(csvSet(profile.getInjuryAreas()));
        injuryAreaOptionRepo.findAll().stream()
                .filter(option -> injuries.contains(option.getCode().toUpperCase(Locale.ROOT)))
                .map(InjuryAreaOption::getLabel)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .forEach(injuries::add);
        Set<String> contraindications = new HashSet<>(csvSet(ex.getContraindicatedInjuries()));
        Set<String> secondaryGroups = new HashSet<>(csvSet(ex.getSecondaryMuscleGroups()));
        Set<String> standardMuscleGroups = Arrays.stream(MuscleGroup.values())
                .map(Enum::name).collect(Collectors.toSet());
        secondaryGroups.removeAll(standardMuscleGroups);
        contraindications.addAll(secondaryGroups);
        if (!Collections.disjoint(injuries, contraindications)) return false;

        // Dự phòng cho các bài tập cũ chưa được admin khai báo vùng chấn thương cần tránh.
        if (injuries.contains("KNEE") && containsAny(name, "squat", "lunge", "leg press", "jump")) return false;
        if (injuries.contains("LOWER_BACK") && containsAny(name, "deadlift", "good morning", "back extension")) return false;
        if (injuries.contains("SHOULDER") && containsAny(name, "shoulder press", "overhead press", "lateral raise", "dip")) return false;
        if (injuries.contains("WRIST") && containsAny(name, "push up", "push-up", "bench press", "plank")) return false;
        if (injuries.contains("ELBOW") && containsAny(name, "curl", "tricep", "dip")) return false;
        if (injuries.contains("ANKLE") && containsAny(name, "jump", "calf raise", "lunge", "running")) return false;
        if (injuries.contains("NECK") && containsAny(name, "shrug", "neck")) return false;

        Set<String> equipment = csvSet(profile.getAvailableEquipment());
        if (equipment.isEmpty() || "GYM".equals(profile.getTrainingLocation())
                || "BOTH".equals(profile.getTrainingLocation())) return true;
        String required = inferRequiredEquipment(name);
        return "BODYWEIGHT".equals(required) || equipment.contains(required);
    }

    private Set<String> csvSet(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(",")).map(String::trim).map(String::toUpperCase)
                .filter(s -> !s.isBlank()).collect(Collectors.toSet());
    }

    private boolean containsAny(String text, String... values) {
        return Arrays.stream(values).anyMatch(text::contains);
    }

    private String inferRequiredEquipment(String name) {
        if (containsAny(name, "cable", "pulldown", "pushdown")) return "CABLE";
        if (containsAny(name, "barbell", "deadlift")) return "BARBELL";
        if (name.contains("dumbbell")) return "DUMBBELL";
        if (name.contains("bench press")) return "BENCH";
        if (containsAny(name, "leg press", "machine", "leg curl", "leg extension")) return "MACHINE";
        if (containsAny(name, "pull up", "pull-up", "chin up", "chin-up")) return "PULL_UP_BAR";
        return "BODYWEIGHT";
    }

    private int getGoalScore(Exercise ex, Goal goal) {
        return switch (goal) {
            case MUSCLE_GAIN -> ex.getMuscleGainScore()   != null ? ex.getMuscleGainScore()   : 0;
            case WEIGHT_LOSS -> ex.getWeightLossScore()   != null ? ex.getWeightLossScore()   : 0;
            case ENDURANCE   -> ex.getFlexibilityScore()    != null ? ex.getFlexibilityScore()    : 0;
            default          -> ex.getMaintenanceScore()  != null ? ex.getMaintenanceScore()  : 0;
        };
    }

    private int adjustDuration(int base, FitnessLevel lv) {
        return switch (lv) {
            case BEGINNER -> (int) (base * systemConfigService.get("EXERCISE_DURATION_BEGINNER", 0.7));
            case ADVANCED -> (int) (base * systemConfigService.get("EXERCISE_DURATION_ADVANCED", 1.3));
            default -> base;
        };
    }

    private int calcRest(Integer base, Goal goal) {
        if (base == null) base = 60;
        return switch (goal) {
            case MUSCLE_GAIN -> (int) (base * systemConfigService.get("REST_MULTIPLIER_MUSCLE_GAIN", 1.3));
            case WEIGHT_LOSS -> (int) (base * systemConfigService.get("REST_MULTIPLIER_WEIGHT_LOSS", 0.7));
            default -> base;
        };
    }

    public List<Integer> suggestDays(int sessions) {
        return trainingConfigService.recommendedDays(sessions);
    }

    private String buildScheduleNote(Goal goal, int sessions) {
        return switch (goal) {
            case MUSCLE_GAIN -> "💪 Tăng cơ cần nghỉ ít nhất 1 ngày giữa các buổi tập nhóm cơ giống nhau.";
            case WEIGHT_LOSS -> "🔥 Giảm cân hiệu quả khi tập liên tục.";
            case ENDURANCE -> "🏃 Sức bền cần xen kẽ ngày cardio và phục hồi.";
            default -> "⚖️ Duy trì đều đặn.";
        };
    }

    private String buildNote(Exercise ex, Goal goal) {
        int score = switch (goal) {
            case MUSCLE_GAIN -> ex.getMuscleGainScore() != null ? ex.getMuscleGainScore() : 0;
            case WEIGHT_LOSS -> ex.getWeightLossScore() != null ? ex.getWeightLossScore() : 0;
            case ENDURANCE ->ex.getFlexibilityScore() != null ? ex.getFlexibilityScore() : 0;
            default -> ex.getMaintenanceScore() != null ? ex.getMaintenanceScore() : 0;
        };
        if (score >= 9) return "⭐ Hàng đầu cho mục tiêu này";
        if (score >= 7) return "✅ Phù hợp tốt";
        return null;
    }

    // ─────────────────────────────────────────────────────────
    // Response Builders
    // ─────────────────────────────────────────────────────────
    public WorkoutPlanResponse toPlanResponse(WorkoutPlan plan, UserProfile profile) {
        List<WorkoutPlanDayResponse> days = Optional.ofNullable(plan.getPlanDays())
                .orElse(Collections.emptyList()).stream()
                .map(this::buildDayResponse).collect(Collectors.toList());

        List<Integer> suggested = Boolean.TRUE.equals(plan.getIsAiGenerated())
                ? trainingConfigService.recommendedDays(plan.getSessionsPerWeek())
                : null;
        String note = buildScheduleNote(plan.getGoal(), plan.getSessionsPerWeek());
        LowCompletionContext lowCompletion = plan.getUser() != null
                ? findLowCompletionContext(plan.getUser()) : new LowCompletionContext();

        Double liveTargetCurrentValue = plan.getTargetCurrentValue();
        if (plan.getGoal() == Goal.ENDURANCE && plan.getUser() != null && plan.getTargetMetricType() != null) {
            AssessmentMetricType metricType = plan.getTargetMetricType();
            liveTargetCurrentValue = enduranceTestRepo.findByUserId(plan.getUser().getId())
                    .map(test -> switch (metricType) {
                        case PUSHUP_REPS -> test.getPushupReps() != null ? test.getPushupReps().doubleValue() : null;
                        case PLANK_SECONDS -> test.getPlankSeconds() != null ? test.getPlankSeconds().doubleValue() : null;
                        case SQUAT_REPS -> test.getSquatReps() != null ? test.getSquatReps().doubleValue() : null;
                    })
                    .orElse(null);
        }

        return WorkoutPlanResponse.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .description(plan.getDescription())
                .goal(plan.getGoal())
                .targetLevel(plan.getTargetLevel())
                .durationWeeks(plan.getDurationWeeks())
                .sessionsPerWeek(plan.getSessionsPerWeek())
                .currentWeek(plan.getCurrentWeek())
                .isActive(plan.getIsActive())
                .isAiGenerated(plan.getIsAiGenerated())
                .isTemplate(plan.getIsTemplate())
                .isCompleted(plan.getIsCompleted())
                .weekStartDate(plan.getWeekStartDate())
                .createdAt(plan.getCreatedAt())
                .startingBmi(plan.getStartingBmi())
                .startingWeight(plan.getStartingWeight())
                .difficultyAdjustment(plan.getDifficultyAdjustment())
                .setsAdjustment(plan.getSetsAdjustment())
                .repsAdjustment(plan.getRepsAdjustment())
                .planDays(days)
                .weightAdjustmentNote(plan.getWeightAdjustmentNote())
                .suggestedDays(suggested)
                .scheduleNote(Boolean.TRUE.equals(plan.getIsAiGenerated()) ? note : null)
                .maxMana(plan.getMaxMana())
                .currentMana(plan.getCurrentMana())
                .manaMessage(plan.getMaxMana() != null
                        ? ManaMessageHelper.buildMessage(plan.getMaxMana(), plan.getMaxMana())
                        : null)
                .fitnessScore(plan.getFitnessScore())
                .fitnessLevel(plan.getFitnessLevel() != null ? plan.getFitnessLevel().name() : null)
                .bodyType(plan.getBodyType() != null ? plan.getBodyType().name() : null)
                .targetMetricType(plan.getTargetMetricType() != null ? plan.getTargetMetricType().name() : null)
                .targetBaselineValue(plan.getTargetBaselineValue())
                .targetGoalValue(plan.getTargetGoalValue())
                .targetCurrentValue(liveTargetCurrentValue)
                .targetAchieved(plan.getTargetAchieved())
                .estimatedWeeks(plan.getEstimatedWeeks())
                .originalPlanId(plan.getOriginalPlanId())
                .isFitnessImprovement(plan.getIsFitnessImprovement())
                .requiredMaxSessionManaCost(plan.getRequiredMaxSessionManaCost())
                .lowCompletionWarning(lowCompletion.triggered)
                .lowCompletionWeek1(lowCompletion.week1)
                .lowCompletionRate1(lowCompletion.rate1)
                .lowCompletionWeek2(lowCompletion.week2)
                .lowCompletionRate2(lowCompletion.rate2)
                .lowCompletionMessage(lowCompletion.triggered
                        ? "Hai tuần gần nhất đều dưới 40%. Bạn có thể cập nhật số liệu để tạo giáo án nhẹ và phù hợp hơn, hoặc tiếp tục giáo án hiện tại."
                        : null)
                .build();
    }

    private LowCompletionContext findLowCompletionContext(User user) {
        LowCompletionContext result = new LowCompletionContext();
        if (user == null || membershipService.isVip(user)) return result;
        WorkoutPlan active = planRepo.findByUserIdAndIsActiveTrue(user.getId()).orElse(null);
        if (active == null || active.getId() == null) return result;

        Map<Integer, List<WorkoutSession>> byWeek = sessionRepo.findByPlanOrderBySessionDate(user.getId(), active.getId())
                .stream().filter(s -> s.getWeekNumber() != null)
                .collect(Collectors.groupingBy(WorkoutSession::getWeekNumber));
        List<Map.Entry<Integer, Integer>> finishedWeeks = byWeek.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty() && e.getValue().stream().allMatch(s ->
                        s.getStatus() == SessionStatus.COMPLETED || s.getStatus() == SessionStatus.SKIPPED))
                .map(e -> Map.entry(e.getKey(), (int) Math.round(e.getValue().stream()
                        .mapToInt(s -> s.getStatus() == SessionStatus.COMPLETED
                                ? Optional.ofNullable(s.getCompletionRate()).orElse(0) : 0)
                        .average().orElse(0))))
                .sorted(Map.Entry.<Integer, Integer>comparingByKey().reversed())
                .limit(2).collect(Collectors.toList());

        if (finishedWeeks.size() == 2 && finishedWeeks.get(0).getKey() - finishedWeeks.get(1).getKey() == 1
                && finishedWeeks.get(0).getValue() < 40 && finishedWeeks.get(1).getValue() < 40) {
            Map.Entry<Integer, Integer> older = finishedWeeks.get(1), newer = finishedWeeks.get(0);
            result.triggered = true; result.week1 = older.getKey(); result.rate1 = older.getValue();
            result.week2 = newer.getKey(); result.rate2 = newer.getValue();
        }
        return result;
    }

    private FitnessLevel lowerLevel(FitnessLevel level) {
        if (level == FitnessLevel.ADVANCED) return FitnessLevel.INTERMEDIATE;
        return FitnessLevel.BEGINNER;
    }

    private void reducePlanLoad(List<WorkoutPlanDay> days) {
        for (WorkoutPlanDay day : days) {
            for (WorkoutPlanExercise exercise : Optional.ofNullable(day.getExercises()).orElse(Collections.emptyList())) {
                if (exercise.getSets() != null) exercise.setSets(Math.max(1, exercise.getSets() - 1));
                if (exercise.getReps() != null) exercise.setReps(Math.max(1, (int) Math.round(exercise.getReps() * 0.8)));
                if (exercise.getDurationSeconds() != null)
                    exercise.setDurationSeconds(Math.max(10, (int) Math.round(exercise.getDurationSeconds() * 0.8)));
            }
        }
    }

    private static class LowCompletionContext {
        boolean triggered;
        Integer week1, rate1, week2, rate2;
    }

    private WorkoutPlanDayResponse buildDayResponse(WorkoutPlanDay day) {
        return WorkoutPlanDayResponse.builder()
                .id(day.getId())
                .dayOfWeek(day.getDayOfWeek())
                .dayName(day.getDayName())
                .exercises(Optional.ofNullable(day.getExercises())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::buildExResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private WorkoutPlanExerciseResponse buildExResponse(WorkoutPlanExercise pe) {
        Exercise ex = pe.getExercise();
        boolean justRevealed = pe.getWeightUpdatedWeek() != null
                && pe.getPlanDay() != null
                && pe.getPlanDay().getWorkoutPlan() != null
                && pe.getWeightUpdatedWeek().equals(pe.getPlanDay().getWorkoutPlan().getCurrentWeek());

        return WorkoutPlanExerciseResponse.builder()
                .id(pe.getId())
                .exerciseId(ex.getId())
                .exerciseName(ex.getName())
                .muscleGroup(ex.getMuscleGroup() != null ? ex.getMuscleGroup().name() : null)
                .difficulty(ex.getDifficulty() != null ? ex.getDifficulty().name() : null)
                .sets(pe.getSets())
                .reps(pe.getReps())
                .durationSeconds(pe.getDurationSeconds())
                .restSeconds(pe.getRestSeconds())
                .orderIndex(pe.getOrderIndex())
                .notes(pe.getNotes())
                .videoUrl(ex.getVideoUrl())
                .description(ex.getDescription())
                .caloriesBurned(ex.getCaloriesBurned())
                .baseWeightKg(pe.getBaseWeightKg())
                .currentWeightKg(pe.getCurrentWeightKg())
                .weightJustRevealed(justRevealed)
                .recommendedWeightKg(pe.getRecommendedWeightKg())
                // ── MỚI ──
                .currentRecommendedWeightKg(pe.getCurrentRecommendedWeightKg())
                .build();
    }

    private String buildPlanName(Goal goal, FitnessLevel lv) {
        String g = switch (goal) {
            case WEIGHT_LOSS -> "Fat Burning";
            case MUSCLE_GAIN -> "Muscle Building";
            case ENDURANCE -> "Endurance";
            default -> "Balanced";
        };
        String l = switch (lv) {
            case BEGINNER -> "Starter";
            case ADVANCED -> "Elite";
            default -> "Progress";
        };
        return g + " " + l + " Plan";
    }

    private String buildPlanDesc(Goal goal, FitnessLevel lv, int days, UserProfile profile) {
        String gv = switch (goal) {
            case WEIGHT_LOSS -> "giảm cân & đốt mỡ";
            case MUSCLE_GAIN -> "tăng cơ & sức mạnh";
            case ENDURANCE -> "tăng sức bền";
            default -> "duy trì thể hình";
        };
        String bmiNote = (profile != null && profile.getBmi() != null)
                ? " (BMI hiện tại: " + profile.getBmi() + ")" : "";
        return String.format("Giáo án cá nhân hóa cho mục tiêu %s%s. %d buổi/tuần.",
                gv, bmiNote, days);
    }

    private User getUser(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void deactivateAndCleanOldPlan(Long userId) {
        planRepo.findByUserIdAndIsActiveTrue(userId).ifPresent(oldPlan -> {
            List<WorkoutSession> oldSessions = sessionRepo
                    .findByUserIdAndWorkoutPlanId(userId, oldPlan.getId());
            if (!oldSessions.isEmpty()) {
                sessionRepo.deleteAll(oldSessions);
            }
            oldPlan.setIsActive(false);
            planRepo.save(oldPlan);
        });
    }

    public WorkoutPlanResponse buildPlanResponse(WorkoutPlan plan) {
        return toPlanResponse(plan, null);
    }

    /** Tính Mana Cost lớn nhất trong các buổi tập của 1 giáo án — dùng estimateSessionCost()
     *  đã có sẵn trong ManaService (đang dùng cho computeManaWarning() ở WorkoutSessionService). */
    private int computeMaxSessionManaCost(List<WorkoutPlanDay> days) {
        int max = 0;
        if (days == null) return max;
        for (WorkoutPlanDay day : days) {
            List<Integer> costs = day.getExercises() == null
                    ? Collections.emptyList()
                    : day.getExercises().stream()
                    .map(pe -> pe.getExercise() != null ? pe.getExercise().getStaminaCost() : null)
                    .collect(Collectors.toList());
            int cost = manaService.estimateSessionCost(costs);
            if (cost > max) max = cost;
        }
        return max;
    }

    /** Pause plan đang active — CHỈ đổi isActive, không đụng WorkoutSession,
     *  currentWeek, weekStartDate, mana hay bất kỳ dữ liệu nào khác.
     *  Không dùng deactivateAndCleanOldPlan() vì hàm đó xóa WorkoutSession. */
    private void pauseActivePlan(Long userId) {
        planRepo.findByUserIdAndIsActiveTrue(userId).ifPresent(oldPlan -> {
            oldPlan.setIsActive(false);
            planRepo.save(oldPlan);
        });
    }
}

package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.request.*;
import com.example.gymmanagement.dto.response.*;
import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.AssessmentMetricType;
import com.example.gymmanagement.enums.Goal;
import com.example.gymmanagement.enums.MuscleGroup;
import com.example.gymmanagement.enums.ProgressSource;
import com.example.gymmanagement.enums.SessionStatus;
import com.example.gymmanagement.pet.PetService;
import com.example.gymmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import com.example.gymmanagement.service.schedule.ScheduleCatalog;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutSessionService {

    private final WorkoutSessionRepository  sessionRepo;
    private final SessionExerciseLogRepository logRepo;
    private final ExerciseRepository        exerciseRepo;
    private final UserRepository            userRepo;
    private final WorkoutPlanRepository     planRepo;
    private final WorkoutPlanDayRepository  dayRepo;
    private final NotificationService       notifService;
    private final UserProfileRepository     profileRepo;
    private final ProgressService progressService;
    private WorkoutPlanService workoutPlanService;
    private final EnduranceTestRepository enduranceTestRepo;
    @org.springframework.beans.factory.annotation.Autowired
    public void setWorkoutPlanService(@Lazy WorkoutPlanService workoutPlanService) {
        this.workoutPlanService = workoutPlanService;
    }
    private PetService petService;
    @org.springframework.beans.factory.annotation.Autowired
    public void setPetService(@Lazy PetService petService) {
        this.petService = petService;
    }

    private final ManaService manaService;
    private final WorkoutPlanMuscleGroupWeightRepository mgWeightRepo;
    private final WorkoutPlanExerciseRepository planExerciseRepo;
    private final MembershipService membershipService;

    @Transactional
    public WorkoutSessionResponse enrollSession(String email, EnrollSessionRequest req) {
        User user = getUser(email);

        WorkoutPlanDay planDay = null;
        WorkoutPlan plan = null;

        if (req.getPlanDayId() != null) {
            planDay = dayRepo.findById(req.getPlanDayId())
                    .orElseThrow(() -> new RuntimeException("Ngày tập không tồn tại"));
            plan = planDay.getWorkoutPlan();

            if (req.getWeekNumber() != null &&
                    sessionRepo.existsByUserIdAndPlanDayIdAndWeekNumber(
                            user.getId(), req.getPlanDayId(), req.getWeekNumber()))
                throw new RuntimeException("Bạn đã đăng ký ngày tập này trong tuần " + req.getWeekNumber());

            if (req.getWeekNumber() != null && plan != null) {
                if (sessionRepo.existsByUserIdAndWorkoutPlanIdAndWeekNumberAndStatus(
                        user.getId(), plan.getId(), req.getWeekNumber(), SessionStatus.SCHEDULED)) {
                    throw new RuntimeException("Bạn đang có một buổi tập chưa checkout. Hãy hoàn thành hoặc thoát buổi đó trước khi bắt đầu buổi khác.");
                }
                long enrolled = sessionRepo.countEnrolledInWeek(user.getId(), plan.getId(), req.getWeekNumber());
                if (enrolled >= plan.getSessionsPerWeek())
                    throw new RuntimeException("Đã đủ " + plan.getSessionsPerWeek() + " buổi cho tuần này!");
            }
        } else if (req.getPlanId() != null) {
            plan = planRepo.findById(req.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Giáo án không tồn tại"));
        }

        if (plan != null && plan.getWeekStartDate() == null && req.getWeekNumber() == 1) {
            plan.setWeekStartDate(req.getSessionDate());
            planRepo.save(plan);
        }

        boolean isLast = Boolean.TRUE.equals(req.getIsLastSessionOfWeek());
        if (!isLast && plan != null && req.getWeekNumber() != null) {
            long current = sessionRepo.countEnrolledInWeek(user.getId(), plan.getId(), req.getWeekNumber());
            isLast = (current + 1) >= plan.getSessionsPerWeek();
        }

        WorkoutSession session = WorkoutSession.builder()
                .user(user).workoutPlan(plan).planDay(planDay)
                .sessionDate(req.getSessionDate()).scheduledTime(req.getScheduledTime())
                .weekNumber(req.getWeekNumber())
                .isLastSessionOfWeek(isLast)
                .customSessionName(req.getCustomSessionName())
                .isCustom(req.getPlanDayId() == null)
                .status(SessionStatus.SCHEDULED)
                .build();
        sessionRepo.save(session);

        String name    = planDay != null ? planDay.getDayName()
                : (req.getCustomSessionName() != null ? req.getCustomSessionName() : "Buổi tập");
        String timeStr = req.getScheduledTime() != null ? " lúc " + req.getScheduledTime() : "";
        notifService.sendToUser(user.getId(), "📅 Đã đăng ký lịch tập",
                "\"" + name + "\" vào " + req.getSessionDate() + timeStr, "WORKOUT_REMINDER");

        return buildResponse(session);
    }

    public Map<String, Object> getWeekProgress(String email, Long planId, Integer weekNumber) {
        User user = getUser(email);
        WorkoutPlan plan = planRepo.findById(planId).orElseThrow();
        long enrolled  = sessionRepo.countEnrolledInWeek(user.getId(), planId, weekNumber);
        long completed = sessionRepo.countCompletedInWeek(user.getId(), planId, weekNumber);
        int  target    = plan.getSessionsPerWeek();
        Double avgRate = sessionRepo.avgCompletionRateInWeek(user.getId(), planId, weekNumber);

        boolean lastCheckedOut = sessionRepo.findLastSessionOfWeek(user.getId(), planId, weekNumber)
                .stream().anyMatch(s -> s.getStatus() == SessionStatus.COMPLETED && s.getCheckoutWeight() != null);

        Map<String, Object> r = new java.util.LinkedHashMap<>();
        r.put("weekNumber",       weekNumber);
        r.put("enrolled",         enrolled);
        r.put("completed",        completed);
        r.put("target",           target);
        r.put("isWeekDone",       completed >= target);
        r.put("canGoNextWeek",    completed >= target && lastCheckedOut);
        r.put("avgCompletionRate",avgRate);
        r.put("currentPlanWeek",  plan.getCurrentWeek());
        r.put("totalWeeks",       plan.getDurationWeeks());
        r.put("setsAdj",          plan.getSetsAdjustment());
        r.put("repsAdj",          plan.getRepsAdjustment());
        return r;
    }

    public List<WorkoutSessionResponse> getMySessions(String email) {
        User user = getUser(email);
        java.util.stream.Stream<WorkoutSession> stream = sessionRepo.findByUserIdOrderBySessionDateDesc(user.getId()).stream();
        if (!membershipService.isVip(user)) {
            LocalDate cutoff = LocalDate.now().minusWeeks(4);
            stream = stream.filter(s -> s.getSessionDate() == null || !s.getSessionDate().isBefore(cutoff));
        }
        return stream.map(this::buildResponse).collect(Collectors.toList());
    }

    public List<WorkoutSessionResponse> getWeekSessions(String email) {
        User u = getUser(email);
        LocalDate mon = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        return sessionRepo.findByUserIdAndSessionDateBetweenOrderBySessionDate(u.getId(), mon, mon.plusDays(6))
                .stream().map(this::buildResponse).collect(Collectors.toList());
    }

    public WorkoutSessionResponse getSessionById(String email, Long id) {
        return buildResponse(getOwned(email, id));
    }

    @Transactional
    public WorkoutSessionResponse checkOut(String email, Long id, CheckOutRequest req) {
        User user = getUser(email);
        WorkoutSession s = getOwned(email, id);

        if (s.getStatus() == SessionStatus.COMPLETED)
            throw new RuntimeException("Buổi tập đã hoàn thành!");
        if (req.getExerciseLogs() == null || req.getExerciseLogs().isEmpty())
            throw new RuntimeException("Vui lòng nhập dữ liệu cho bài tập!");

        WorkoutPlan plan = s.getWorkoutPlan();
        // ── MỚI: khai báo sớm để dùng cho cả điều kiện Assessment bên dưới và
        // rẽ nhánh advanceTemplatePlanWeek/adjustPlanAfterWeek phía sau (không đổi) ──
        boolean isAiPlan = plan != null && Boolean.TRUE.equals(plan.getIsAiGenerated());

        boolean isLastCompletedSession = isLastCompletedSessionOfWeek(user.getId(), plan, s);

        String overLimitWarning = buildOverLimitWarning(s, req.getExerciseLogs());

        boolean isReviewSubmission = req.getCheckoutWeight() != null
                || req.getAssessmentMetricType() != null || req.getAssessmentValue() != null;

        if (isLastCompletedSession && !isReviewSubmission) {
            WorkoutSessionResponse resp = buildResponse(s);
            resp.setNeedWeeklyReview(true);
            resp.setOverLimitWarning(overLimitWarning);
            return resp;
        }

        if (isLastCompletedSession) {
            if (req.getCheckoutWeight() == null)
                throw new RuntimeException("Đây là buổi cuối tuần! Vui lòng nhập cân nặng hiện tại.");
            // ── SỬA: chỉ bắt buộc Assessment cho giáo án AI. Admin có thể chọn
            // Goal.ENDURANCE nhưng không có Target Tracking -> không có Assessment. ──
            if (isAiPlan && plan.getGoal() == Goal.ENDURANCE
                    && (req.getAssessmentMetricType() == null || req.getAssessmentValue() == null)) {
                throw new RuntimeException("Vui lòng nhập kết quả Assessment (mục tiêu Sức bền) trước khi hoàn thành tuần.");
            }
        }

        Map<Long, WorkoutPlanExercise> plannedByExerciseId = (s.getPlanDay() != null && s.getPlanDay().getExercises() != null)
                ? s.getPlanDay().getExercises().stream()
                .collect(Collectors.toMap(pe -> pe.getExercise().getId(), pe -> pe, (a, b) -> a))
                : Collections.emptyMap();

        List<SessionExerciseLog> logs = req.getExerciseLogs().stream().map(r -> {
            Exercise ex = exerciseRepo.findById(r.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("Exercise not found"));
            WorkoutPlanExercise pe = plannedByExerciseId.get(r.getExerciseId());
            Integer cp = computeCompletionPercent(pe, r.getRepsCompleted(), r.getDurationCompleted());

            return SessionExerciseLog.builder()
                    .session(s).exercise(ex)
                    .repsCompleted(r.getRepsCompleted())
                    .durationSeconds(r.getDurationCompleted())
                    .completionPercent(cp)
                    .weightUsedKg(r.getWeightUsedKg())
                    .isCompleted(cp != null && cp > 0)
                    .notes(r.getNotes())
                    .build();
        }).collect(Collectors.toList());
        logRepo.saveAll(logs);

        double avgSessionRate = logs.stream()
                .filter(l -> l.getCompletionPercent() != null)
                .mapToInt(SessionExerciseLog::getCompletionPercent)
                .average().orElse(0);
        int sessionCompletionRate = (int) Math.round(avgSessionRate);
        s.setCompletionRate(sessionCompletionRate);

        int cal = logs.stream()
                .filter(l -> l.getCompletionPercent() != null && l.getCompletionPercent() > 0
                        && l.getExercise().getCaloriesBurned() != null)
                .mapToInt(l -> Math.round(l.getExercise().getCaloriesBurned() * (l.getCompletionPercent() / 100f)))
                .sum();
        s.setTotalCaloriesBurned(cal);

        s.setStatus(SessionStatus.COMPLETED);
        s.setCheckOutTime(LocalDateTime.now());
        s.setNotes(req.getNotes());
        if (req.getCheckoutWeight()  != null) s.setCheckoutWeight(req.getCheckoutWeight());
        if (req.getCheckoutBodyFat() != null) s.setCheckoutBodyFat(req.getCheckoutBodyFat());
        sessionRepo.save(s);

        boolean injuryRisk = false;
        if (plan != null) {
            int totalConsumed = logs.stream()
                    .mapToInt(l -> {
                        int cost = l.getExercise().getStaminaCost() != null ? l.getExercise().getStaminaCost() : 10;
                        int pct  = l.getCompletionPercent() != null ? l.getCompletionPercent() : 0;
                        return Math.round(cost * (pct / 100f));
                    }).sum();
            injuryRisk = manaService.consumeMana(plan, totalConsumed);
            if (injuryRisk) {
                notifService.sendToUser(user.getId(), "⚠️ Cảnh báo chấn thương",
                        "Bạn đã tập vượt quá thể lực hiện có. Hãy cân nhắc nghỉ ngơi để tránh chấn thương.", "SYSTEM");
            }
        }

        boolean isFitnessImprovementPlan = plan != null && Boolean.TRUE.equals(plan.getIsFitnessImprovement());
        boolean isTemplatePlan = plan != null && Boolean.FALSE.equals(plan.getIsAiGenerated())
                && !isFitnessImprovementPlan;

        if (isLastCompletedSession) {

            // ── SỬA: chỉ áp dụng Assessment cho giáo án AI ──
            if (isAiPlan && plan.getGoal() == Goal.ENDURANCE) {
                applyAssessmentFromReview(user, req.getAssessmentMetricType(), req.getAssessmentValue());
            }

            progressService.autoSaveProgress(
                    user, req.getCheckoutWeight(), req.getCheckoutBodyFat(),
                    "Tự động ghi nhận sau tuần " + s.getWeekNumber(),
                    ProgressSource.WEEKLY_CHECKOUT, s.getSessionDate());

            profileRepo.findByUserId(user.getId()).ifPresent(profile -> {
                profile.setWeight(req.getCheckoutWeight());
                if (req.getCheckoutBodyFat() != null) profile.setBodyFatPercentage(req.getCheckoutBodyFat());
                profileRepo.save(profile);
            });

            if (membershipService.isVip(user)) {
                applyWeightAdjustmentNote(user, plan, s.getWeekNumber(), req.getCheckoutWeight());
                List<SessionExerciseLog> weekLogs = logRepo.findByUserIdAndPlanIdAndWeekNumber(
                        user.getId(), plan.getId(), s.getWeekNumber());
                adjustMuscleGroupWeights(plan, weekLogs);
            } else {
                plan.setWeightAdjustmentNote("Gói thường: giáo án được giữ nguyên. Nâng cấp VIP để tự động điều chỉnh theo kết quả mỗi tuần.");
                planRepo.save(plan);
            }

            if (isFitnessImprovementPlan) {
                try {
                    workoutPlanService.checkFitnessImprovementProgress(plan, email);
                } catch (Exception e) {
                    log.warn("Không thể kiểm tra tiến độ thể lực sau buổi tập {} của user {}",
                            s.getId(), user.getId(), e);
                }
            } else if (isTemplatePlan) {
                advanceTemplatePlanWeek(plan);
            } else if (isAiPlan && membershipService.isVip(user)) {
                try {
                    workoutPlanService.adjustPlanAfterWeek(
                            plan.getId(), email, req.getCheckoutWeight(), req.getCheckoutBodyFat());
                } catch (Exception e) {
                    log.warn("Không thể căn chỉnh tuần mới sau buổi tập {} của user {}",
                            s.getId(), user.getId(), e);
                }
            } else if (isAiPlan) {
                workoutPlanService.advancePlanWeekWithoutAdjustment(plan.getId(), email);
            }
        }

        String msg = sessionCompletionRate >= 90
                ? "🔥 Xuất sắc! " + sessionCompletionRate + "% hoàn thành!"
                : sessionCompletionRate >= 70
                ? "✅ Tốt! " + sessionCompletionRate + "% hoàn thành."
                : "💪 " + sessionCompletionRate + "% — cố gắng hơn buổi sau nhé!";
        notifService.sendToUser(user.getId(), "Kết quả buổi tập", msg, "SYSTEM");

        if (isLastCompletedSession) {
            String nextMsg = isTemplatePlan
                    ? "Dữ liệu đã ghi nhận. Giáo án đã chuyển sang tuần tiếp theo."
                    : membershipService.isVip(user)
                    ? "Dữ liệu đã ghi nhận và giáo án VIP đã được tự động căn chỉnh cho tuần mới."
                    : "Dữ liệu đã ghi nhận. Giáo án đã chuyển tuần nhưng không tự điều chỉnh vì bạn đang dùng gói thường.";
            notifService.sendToUser(user.getId(),
                    "📊 Hoàn thành tuần " + s.getWeekNumber() + "!", nextMsg, "SYSTEM");
        }

        try { petService.recalculate(email); } catch (Exception ignored) {}

        WorkoutSessionResponse resp = buildResponse(s);
        resp.setInjuryRisk(injuryRisk);
        resp.setOverLimitWarning(overLimitWarning);
        resp.setNeedWeeklyReview(false);
        return resp;
    }

    private boolean isLastCompletedSessionOfWeek(Long userId, WorkoutPlan plan, WorkoutSession current) {
        if (plan == null || current.getWeekNumber() == null || plan.getSessionsPerWeek() == null) return false;
        long completedOthers = sessionRepo.countCompletedInWeek(userId, plan.getId(), current.getWeekNumber());
        return (completedOthers + 1) >= plan.getSessionsPerWeek();
    }

    private String buildOverLimitWarning(WorkoutSession s, List<ExerciseLogRequest> logs) {
        if (s.getPlanDay() == null || s.getPlanDay().getExercises() == null) return null;
        Map<Long, WorkoutPlanExercise> plannedByExerciseId = s.getPlanDay().getExercises().stream()
                .collect(Collectors.toMap(pe -> pe.getExercise().getId(), pe -> pe, (a, b) -> a));

        for (ExerciseLogRequest log : logs) {
            WorkoutPlanExercise pe = plannedByExerciseId.get(log.getExerciseId());
            if (pe == null || pe.getSets() == null) continue;

            if (pe.getReps() != null && log.getRepsCompleted() != null) {
                int plannedTotal = pe.getSets() * pe.getReps();
                if (plannedTotal > 0 && log.getRepsCompleted() > plannedTotal * 1.5) {
                    return "Bạn đã tập vượt quá 150% khối lượng bài tập. Điều này sẽ không tốt cho sức khỏe.";
                }
            } else if (pe.getDurationSeconds() != null && log.getDurationCompleted() != null) {
                int plannedTotal = pe.getSets() * pe.getDurationSeconds();
                if (plannedTotal > 0 && log.getDurationCompleted() > plannedTotal * 1.5) {
                    return "Bạn đã tập vượt quá 150% khối lượng bài tập. Điều này sẽ không tốt cho sức khỏe.";
                }
            }
        }
        return null;
    }

    private Integer computeCompletionPercent(WorkoutPlanExercise pe, Integer repsCompleted, Integer durationCompleted) {
        if (pe == null) return null;

        double rawPercent;
        if (pe.getReps() != null) {
            if (pe.getSets() == null || repsCompleted == null) return null;
            int plannedTotalReps = pe.getSets() * pe.getReps();
            if (plannedTotalReps <= 0) return null;
            rawPercent = (repsCompleted / (double) plannedTotalReps) * 100;
        } else if (pe.getDurationSeconds() != null) {
            if (pe.getSets() == null || durationCompleted == null) return null;
            int plannedTotalDuration = pe.getSets() * pe.getDurationSeconds();
            if (plannedTotalDuration <= 0) return null;
            rawPercent = (durationCompleted / (double) plannedTotalDuration) * 100;
        } else {
            return null;
        }

        double clamped = Math.max(0, Math.min(200, rawPercent));
        return (int) Math.round(clamped);
    }

    private void applyAssessmentFromReview(User user, AssessmentMetricType type, Integer value) {
        if (type == null || value == null) return;
        EnduranceTest test = enduranceTestRepo.findByUserId(user.getId())
                .orElseGet(() -> EnduranceTest.builder().user(user).build());
        switch (type) {
            case PUSHUP_REPS -> test.setPushupReps(value);
            case PLANK_SECONDS -> test.setPlankSeconds(value);
            case SQUAT_REPS -> test.setSquatReps(value);
        }
        enduranceTestRepo.save(test);
    }

    @Transactional
    public WorkoutSessionResponse skipSession(String email, Long id, String notes) {
        WorkoutSession s = getOwned(email, id);
        s.setStatus(SessionStatus.SKIPPED); s.setNotes(notes);
        sessionRepo.save(s);
        return buildResponse(s);
    }

    @Transactional
    public void deleteSession(String email, Long id) {
        WorkoutSession s = getOwned(email, id);
        if (s.getStatus() == SessionStatus.COMPLETED)
            throw new RuntimeException("Không thể xóa buổi đã hoàn thành");
        sessionRepo.delete(s);
    }

    // ── Build response ────────────────────────────────────────
    public WorkoutSessionResponse buildResponse(WorkoutSession s) {
        List<ExerciseLogResponse> logs = logRepo.findBySessionId(s.getId()).stream().map(l ->
                ExerciseLogResponse.builder()
                        .id(l.getId()).exerciseId(l.getExercise().getId())
                        .exerciseName(l.getExercise().getName())
                        .setsCompleted(l.getSetsCompleted()).repsCompleted(l.getRepsCompleted())
                        .durationSeconds(l.getDurationSeconds()).weightUsedKg(l.getWeightUsedKg())
                        .isCompleted(l.getIsCompleted())
                        .completionPercent(l.getCompletionPercent())
                        .notes(l.getNotes()).build()
        ).collect(Collectors.toList());

        List<WorkoutPlanExerciseResponse> planExs = Collections.emptyList();
        if (s.getPlanDay() != null && s.getPlanDay().getExercises() != null) {
            planExs = s.getPlanDay().getExercises().stream().map(pe -> {
                boolean justRevealed = pe.getWeightUpdatedWeek() != null
                        && pe.getPlanDay() != null && pe.getPlanDay().getWorkoutPlan() != null
                        && pe.getWeightUpdatedWeek().equals(pe.getPlanDay().getWorkoutPlan().getCurrentWeek());
                return WorkoutPlanExerciseResponse.builder()
                        .id(pe.getId()).exerciseId(pe.getExercise().getId())
                        .exerciseName(pe.getExercise().getName())
                        .muscleGroup(pe.getExercise().getMuscleGroup()!=null ? pe.getExercise().getMuscleGroup().name() : null)
                        .difficulty(pe.getExercise().getDifficulty()!=null   ? pe.getExercise().getDifficulty().name()  : null)
                        .sets(pe.getSets())
                        .reps(pe.getReps())
                        .durationSeconds(pe.getDurationSeconds())
                        .restSeconds(pe.getRestSeconds()).orderIndex(pe.getOrderIndex())
                        .notes(pe.getNotes())
                        .videoUrl(pe.getExercise().getVideoUrl())
                        .caloriesBurned(pe.getExercise().getCaloriesBurned())
                        .baseWeightKg(pe.getBaseWeightKg())
                        .currentWeightKg(pe.getCurrentWeightKg())
                        .weightJustRevealed(justRevealed)
                        .recommendedWeightKg(pe.getRecommendedWeightKg())
                        .currentRecommendedWeightKg(pe.getCurrentRecommendedWeightKg())
                        .build();
            }).collect(Collectors.toList());
        }


        return WorkoutSessionResponse.builder()
                .id(s.getId()).sessionDate(s.getSessionDate()).scheduledTime(s.getScheduledTime())
                .checkInTime(s.getCheckInTime()).checkOutTime(s.getCheckOutTime())
                .status(s.getStatus()).totalCaloriesBurned(s.getTotalCaloriesBurned())
                .durationMinutes(s.getDurationMinutes()).notes(s.getNotes())
                .weekNumber(s.getWeekNumber())
                .planId(s.getWorkoutPlan() != null ? s.getWorkoutPlan().getId() : null)
                .planName(s.getWorkoutPlan()!=null ? s.getWorkoutPlan().getPlanName() : null)
                .dayName(s.getPlanDay()!=null       ? s.getPlanDay().getDayName()    : null)
                .customSessionName(s.getCustomSessionName()).isCustom(s.getIsCustom())
                .completionRate(s.getCompletionRate())
                .isLastSessionOfWeek(s.getIsLastSessionOfWeek())
                .checkoutWeight(s.getCheckoutWeight()).checkoutBodyFat(s.getCheckoutBodyFat())
                .exerciseLogs(logs).planExercises(planExs)
                .orderWarning(buildOrderWarning(s))
                .scheduleWarning(buildScheduleWarning(s))
                .build();
    }

    private String computeOrderWarning(Long userId, WorkoutPlan plan, WorkoutPlanDay targetDay, Integer weekNumber) {
        if (plan == null || targetDay == null || weekNumber == null) return null;
        // ── XOÁ: if (!Boolean.TRUE.equals(plan.getIsAiGenerated())) return null;
        // OrderWarning giờ áp dụng chung cho cả AI và Admin ──

        List<WorkoutPlanDay> days = dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(plan.getId());
        if (days == null || days.isEmpty()) return null;

        long completedCount = sessionRepo.countCompletedInWeek(userId, plan.getId(), weekNumber);
        if (completedCount >= days.size()) return null;

        WorkoutPlanDay expectedDay = days.get((int) completedCount);
        if (!expectedDay.getId().equals(targetDay.getId())) {
            return "Thứ tự buổi tập không đúng.";
        }
        return null;
    }

    private String computeScheduleWarning(Long userId, WorkoutPlan plan, LocalDate sessionDate) {
        if (plan == null || sessionDate == null) return null;
        if (plan.getSessionsPerWeek() == null) return null;

        int actualDow = sessionDate.getDayOfWeek().getValue();
        boolean isAi = Boolean.TRUE.equals(plan.getIsAiGenerated());
        boolean dayMismatch;

        if (isAi) {
            // AI: kiểm tra theo lịch khuyến nghị của hệ thống
            List<Integer> recommended = ScheduleCatalog.recommendedFor(plan.getSessionsPerWeek());
            dayMismatch = !recommended.contains(actualDow);
        } else {
            // Admin: kiểm tra theo đúng ngày Admin đã cấu hình
            List<Integer> configuredDows = dayRepo
                    .findByWorkoutPlanIdOrderByDayOfWeek(plan.getId())
                    .stream()
                    .map(WorkoutPlanDay::getDayOfWeek)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if (configuredDows.isEmpty()) return null;

            dayMismatch = !configuredDows.contains(actualDow);
        }


        if (dayMismatch) {
            return isAi
                    ? "Không tập đúng lịch khuyến nghị của hệ thống."
                    : "Không tập đúng lịch khuyến nghị của Admin.";
        }
        return null;
    }

    // ── MỚI: cảnh báo Mana không đủ cho TOÀN BỘ buổi tập, tính lúc "Bắt đầu tập"
    // (KHÔNG dùng injuryRisk — injuryRisk chỉ tính được SAU Check-out dựa trên
    // completionPercent thực tế). Chỉ cảnh báo, không chặn. ──
    private String computeManaWarning(WorkoutPlanDay day, WorkoutPlan plan) {
        if (plan == null || plan.getMaxMana() == null) return null;
        if (day == null || day.getExercises() == null || day.getExercises().isEmpty()) return null;

        List<Integer> staminaCosts = day.getExercises().stream()
                .map(pe -> pe.getExercise() != null ? pe.getExercise().getStaminaCost() : null)
                .collect(Collectors.toList());

        int estimatedCost = manaService.estimateSessionCost(staminaCosts);
        int currentMana = manaService.getCurrentManaAfterRegen(plan);

        if (estimatedCost > currentMana) {
            return "Mức độ sẵn sàng tập luyện không đủ.";
        }
        return null;
    }


    private String buildScheduleWarning(WorkoutSession s) {
        if (s.getWorkoutPlan() == null || s.getSessionDate() == null) {
            return null;
        }

        if (s.getStatus() == SessionStatus.COMPLETED) {
            return null;
        }

        return computeScheduleWarning(
                s.getUser().getId(),
                s.getWorkoutPlan(),
                s.getSessionDate()
        );
    }
    private String buildOrderWarning(WorkoutSession s) {
        if (s.getWorkoutPlan() == null || s.getPlanDay() == null || s.getWeekNumber() == null) return null;
        if (s.getStatus() == SessionStatus.COMPLETED) return null;
        return computeOrderWarning(s.getUser().getId(), s.getWorkoutPlan(), s.getPlanDay(), s.getWeekNumber());
    }

    public Map<String, String> checkOrderWarning(String email, Long planDayId, Integer weekNumber, LocalDate sessionDate) {
        User user = getUser(email);
        WorkoutPlanDay day = dayRepo.findById(planDayId)
                .orElseThrow(() -> new RuntimeException("Ngày tập không tồn tại"));
        WorkoutPlan plan = day.getWorkoutPlan();

        Map<String, String> result = new java.util.HashMap<>();
        result.put("orderWarning", computeOrderWarning(user.getId(), plan, day, weekNumber));
        result.put("scheduleWarning", computeScheduleWarning(user.getId(), plan, sessionDate));
        result.put("manaWarning", computeManaWarning(day, plan));
        return result;
    }

    private WorkoutSession getOwned(String email, Long id) {
        User u = getUser(email);
        WorkoutSession s = sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!s.getUser().getId().equals(u.getId())) throw new RuntimeException("Access denied");
        return s;
    }

    private User getUser(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private static final double MAINTENANCE_TOLERANCE_PERCENT = 5.0;
    private static final String MAINTENANCE_WARNING_NOTE =
            "Cân nặng hiện tại đã lệch quá 5% so với thời điểm bắt đầu giáo án. " +
                    "Hệ thống khuyến nghị bạn điều chỉnh chế độ ăn uống hoặc luyện tập để quay về trạng thái duy trì.";

    private void applyWeightAdjustmentNote(User user, WorkoutPlan plan, Integer weekNumber, Double checkoutWeight) {
        if (plan == null) return;

        if (plan.getGoal() == Goal.MAINTENANCE) {
            Double baseline = plan.getTargetBaselineValue();
            if (baseline == null || baseline == 0 || checkoutWeight == null) return;

            double deviationPercent = Math.abs(checkoutWeight - baseline) / baseline * 100;
            String note = deviationPercent > MAINTENANCE_TOLERANCE_PERCENT ? MAINTENANCE_WARNING_NOTE : null;

            plan.setWeightAdjustmentNote(note);
            planRepo.save(plan);
            return;
        }

        Double avgRate = sessionRepo.avgCompletionRateInWeek(user.getId(), plan.getId(), weekNumber);
        if (avgRate == null) return;

        String note;
        if (avgRate > 150) {
            note = "Tỉ lệ hoàn thành tuần trước của bạn là " + Math.round(avgRate) + "%! Hiệu suất rất cao.";
        } else if (avgRate >= 120) {
            note = "Tỉ lệ hoàn thành tuần trước của bạn là " + Math.round(avgRate) + "%. Hiệu suất tốt.";
        } else if (avgRate >= 90) {
            note = "Tỉ lệ hoàn thành tuần trước của bạn là " + Math.round(avgRate) + "%. Hiệu suất ổn định.";
        } else if (avgRate >= 80) {
            note = "Tỉ lệ hoàn thành tuần trước của bạn là " + Math.round(avgRate) + "%. Hiệu xuất khá ổn địmk.";
        } else if (avgRate >= 60) {
            note = "Tỉ lệ hoàn thành tuần trước của bạn là " + Math.round(avgRate) + "%. Hiệu xuất chưa ổn định.";
        } else if (avgRate >= 30) {
            note = "Tỉ lệ hoàn thành tuần trước của bạn là " + Math.round(avgRate) + "%. Hiệu xuất tập chưa tốt hãy chú ý";
        } else {
            note = "Tỉ lệ hoàn thành tuần trước của bạn là " + Math.round(avgRate) + "%. Hiệu xuất tập Luyện không ổn định";
        }

        plan.setWeightAdjustmentNote(note);
        planRepo.save(plan);
    }

    private void adjustMuscleGroupWeights(WorkoutPlan plan, List<SessionExerciseLog> weekLogs) {
        Map<MuscleGroup, List<Integer>> byGroup = weekLogs.stream()
                .filter(l -> l.getCompletionPercent() != null && l.getExercise().getMuscleGroup() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getExercise().getMuscleGroup(),
                        Collectors.mapping(SessionExerciseLog::getCompletionPercent, Collectors.toList())));

        int nextWeek = (plan.getCurrentWeek() != null ? plan.getCurrentWeek() : 1) + 1;

        for (var entry : byGroup.entrySet()) {
            MuscleGroup mg = entry.getKey();
            double avgRate = entry.getValue().stream().mapToInt(i -> i).average().orElse(0);

            double factor = avgRate > 150 ? 1.15
                    : avgRate >= 120 ? 1.10
                    : avgRate >= 90 ? 1.05
                    : avgRate >= 80 ? 1.00
                    : avgRate >= 60 ? 0.95
                    : avgRate >= 30 ? 0.90
                    : 0.80;

            WorkoutPlanMuscleGroupWeight mgw = mgWeightRepo
                    .findByWorkoutPlanIdAndMuscleGroup(plan.getId(), mg)
                    .orElseGet(() -> WorkoutPlanMuscleGroupWeight.builder()
                            .workoutPlan(plan).muscleGroup(mg).multiplier(1.0).build());
            mgw.setMultiplier(mgw.getMultiplier() * factor);
            mgWeightRepo.save(mgw);

            List<WorkoutPlanExercise> exs = planExerciseRepo
                    .findByPlanDay_WorkoutPlan_IdAndExercise_MuscleGroup(plan.getId(), mg);
            for (WorkoutPlanExercise pe : exs) {
                if (pe.getBaseWeightKg() != null) {
                    pe.setCurrentWeightKg(Math.round(pe.getBaseWeightKg() * mgw.getMultiplier() * 10.0) / 10.0);
                    pe.setWeightUpdatedWeek(nextWeek);
                } else if (pe.getRecommendedWeightKg() != null) {
                    double raw = pe.getRecommendedWeightKg() * mgw.getMultiplier();
                    pe.setCurrentRecommendedWeightKg(Math.round(raw * 2) / 2.0);
                }
            }
            planExerciseRepo.saveAll(exs);
        }
    }

    private void advanceTemplatePlanWeek(WorkoutPlan plan) {
        int nextWeek = (plan.getCurrentWeek() != null ? plan.getCurrentWeek() : 1) + 1;
        plan.setCurrentWeek(nextWeek);
        if (nextWeek > plan.getDurationWeeks()) {
            plan.setIsCompleted(true);
            plan.setIsActive(false);
        }
        planRepo.save(plan);
    }
}

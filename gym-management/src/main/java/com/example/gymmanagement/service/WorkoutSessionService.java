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
import com.example.gymmanagement.service.schedule.ScheduleCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;

@Service
@RequiredArgsConstructor
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

    // ── Đăng ký buổi tập / "Bắt đầu tập" (Business Rule mục 4, LOCKED — KHÔNG đổi API) ──
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

        // ── enrollSession() CHỈ tạo + lưu (Business Rule mục 1, LOCKED). Không tính
        // cảnh báo, không xác định buổi cuối, không kiểm tra thứ tự ở đây. buildResponse()
        // (hàm dựng dữ liệu hiển thị, không phải logic tạo session) sẽ tự tính orderWarning. ──
        return buildResponse(session);
    }

    // ── Tiến trình tuần ──────────────────────────────────────
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

    // ── Xem sessions ─────────────────────────────────────────
    public List<WorkoutSessionResponse> getMySessions(String email) {
        return sessionRepo.findByUserIdOrderBySessionDateDesc(getUser(email).getId())
                .stream().map(this::buildResponse).collect(Collectors.toList());
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

        // ── Weekly Review — buổi HOÀN THÀNH cuối cùng của tuần, KHÔNG dùng
        // isLastSessionOfWeek (field đó giờ chỉ còn nghĩa "buổi cuối lúc đăng ký lịch",
        // không liên quan gì tới việc mở Weekly Review — Business Rule mới nhất, LOCKED).
        // Quy tắc: sau khi Session hiện tại được hoàn thành, nếu KHÔNG còn Session nào
        // khác trong tuần đang ở trạng thái SCHEDULED -> đây là buổi hoàn thành cuối cùng. ──
        boolean isLastCompletedSession = isLastCompletedSessionOfWeek(user.getId(), plan, s);

        // ── Cảnh báo tập quá 150% (mục 4) — tính theo TỪNG bài tập, giữ nguyên ──
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
            if (plan != null && plan.getGoal() == Goal.ENDURANCE
                    && (req.getAssessmentMetricType() == null || req.getAssessmentValue() == null)) {
                throw new RuntimeException("Vui lòng nhập kết quả Assessment (mục tiêu Sức bền) trước khi hoàn thành tuần.");
            }
        }

        // ================= TỪ ĐÂY TRỞ XUỐNG: PERSIST TOÀN BỘ TRONG 1 TRANSACTION =================

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
        // ── KHÔNG đụng isLastSessionOfWeek — field này giờ CHỈ mang nghĩa "buổi cuối lúc
        // đăng ký lịch", không dùng cho Weekly Review, giữ nguyên giá trị gốc lúc enroll. ──
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

        boolean isTemplatePlan = plan != null && Boolean.FALSE.equals(plan.getIsAiGenerated());
        boolean isAiPlan       = plan != null && Boolean.TRUE.equals(plan.getIsAiGenerated());

        if (isLastCompletedSession) {

            if (plan != null && plan.getGoal() == Goal.ENDURANCE) {
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

            applyWeightAdjustmentNote(user, plan, s.getWeekNumber(), req.getCheckoutWeight());

            List<SessionExerciseLog> weekLogs = logRepo.findByUserIdAndPlanIdAndWeekNumber(
                    user.getId(), plan.getId(), s.getWeekNumber());
            adjustMuscleGroupWeights(plan, weekLogs);

            if (isTemplatePlan) {
                advanceTemplatePlanWeek(plan);
            } else if (isAiPlan) {
                try {
                    workoutPlanService.adjustPlanAfterWeek(
                            plan.getId(), email, req.getCheckoutWeight(), req.getCheckoutBodyFat());
                } catch (Exception e) {
                    notifService.sendToUser(user.getId(), "⚠️ Lưu ý",
                            "Buổi tập đã hoàn thành nhưng căn chỉnh tuần mới gặp sự cố. Vui lòng thử lại.", "SYSTEM");
                }
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
                    ? "Dữ liệu đã ghi nhận. Giáo án tự động chuyển sang tuần tiếp theo."
                    : "Dữ liệu đã ghi nhận và giáo án đã được căn chỉnh cho tuần mới.";
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

    /** Weekly Review = buổi HOÀN THÀNH cuối cùng của tuần (Business Rule mới nhất, LOCKED).
     *  KHÔNG dùng isLastSessionOfWeek, KHÔNG dùng dayNumber/PlanDay index/thứ tự enroll.
     *  So sánh SỐ LƯỢNG buổi đã COMPLETED thực tế với sessionsPerWeek (số cố định của
     *  giáo án) — hoàn toàn không phụ thuộc buổi nào đã/chưa tồn tại trong DB. ──
     *
     *  SỬA (bugfix): bản cũ dựa vào findByPlanAndWeek() rồi lọc "không còn session nào
     *  SCHEDULED" — nhưng buổi CHƯA được enroll thì KHÔNG hề có row trong DB, nên danh
     *  sách "session khác" có thể trống ngay từ buổi đầu tiên (vacuous truth), khiến
     *  Review mở sai ngay ở buổi đầu tiên bất kể tập buổi nào trước. ──
     */
    private boolean isLastCompletedSessionOfWeek(Long userId, WorkoutPlan plan, WorkoutSession current) {
        if (plan == null || current.getWeekNumber() == null || plan.getSessionsPerWeek() == null) return false;

        long completedOthers = sessionRepo.countCompletedInWeek(userId, plan.getId(), current.getWeekNumber());
        return (completedOthers + 1) >= plan.getSessionsPerWeek();
    }

    /** Cảnh báo tập vượt 150% kế hoạch (mục 7, LOCKED) — chỉ cảnh báo, không chặn.
     *  Tính từ dữ liệu gửi lên MỖI LẦN gọi checkOut(), không phụ thuộc việc có persist hay không. */
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

    /** Ghi Assessment từ Popup Review (mục 10/14, LOCKED) — thay thế hoàn toàn
     *  applyAssessmentResult() cũ (đã xoá, không còn qua Exercise/isAssessment). */
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

        ScheduleCheckInfo scheduleInfo = buildScheduleCheckInfo(s);

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
                .dayMismatchWarning(scheduleInfo != null ? scheduleInfo.warning() : null)
                .scheduleSelectionRequired(scheduleInfo != null && scheduleInfo.selectionRequired())
                .scheduleOptions(scheduleInfo != null ? scheduleInfo.options() : null)
                // ── MỚI: cảnh báo sai thứ tự đề xuất — cơ chế độc lập với DayMismatch ──
                .orderWarning(buildOrderWarning(s))
                .build();
    }

    /** Tính cảnh báo sai thứ tự (mục 3, LOCKED) cho MỘT WorkoutPlanDay cụ thể — KHÔNG
     *  cần WorkoutSession đã tồn tại, KHÔNG ghi gì vào DB. Dùng chung cho bước "kiểm tra
     *  TRƯỚC khi enroll" (mới, bugfix) và buildResponse() (sau khi đã enroll). */
    private String computeOrderWarning(Long userId, WorkoutPlan plan, WorkoutPlanDay targetDay, Integer weekNumber) {
        if (plan == null || targetDay == null || weekNumber == null) return null;
        if (!Boolean.TRUE.equals(plan.getIsAiGenerated())) return null;

        List<WorkoutPlanDay> days = dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(plan.getId());
        if (days == null || days.isEmpty()) return null;

        long completedCount = sessionRepo.countCompletedInWeek(userId, plan.getId(), weekNumber);
        if (completedCount >= days.size()) return null;

        WorkoutPlanDay expectedDay = days.get((int) completedCount);
        if (!expectedDay.getId().equals(targetDay.getId())) {
            return "Tập không đúng lịch đề xuất có thể gây ảnh hướng đến hiệu quả của giáo án" +
                    "Ban muốn tập tiếp không ?";
        }
        return null;
    }

    /** Cảnh báo sai thứ tự cho 1 session ĐÃ tồn tại — giữ để buildResponse() vẫn hiển
     *  thị nhất quán, nhưng KHÔNG còn là điểm quyết định popup ở Frontend nữa (đã
     *  chuyển sang checkOrderWarning() bên dưới, chạy TRƯỚC khi enroll). */
    private String buildOrderWarning(WorkoutSession s) {
        if (s.getWorkoutPlan() == null || s.getPlanDay() == null || s.getWeekNumber() == null) return null;
        if (s.getStatus() == SessionStatus.COMPLETED) return null;
        return computeOrderWarning(s.getUser().getId(), s.getWorkoutPlan(), s.getPlanDay(), s.getWeekNumber());
    }

    /** MỚI (bugfix): kiểm tra thứ tự TRƯỚC khi tạo Session — không persist bất kỳ gì.
     *  Frontend gọi hàm này trước enrollSession(); chỉ khi người dùng xác nhận "Tiếp tục"
     *  (hoặc không có cảnh báo) thì mới thật sự gọi enrollSession(). Nếu người dùng bấm
     *  Huỷ ở bước này, không có gì từng được tạo -> coi như chưa bấm gì cả (đúng yêu cầu). */
    public Map<String, String> checkOrderWarning(String email, Long planDayId, Integer weekNumber) {
        User user = getUser(email);
        WorkoutPlanDay day = dayRepo.findById(planDayId)
                .orElseThrow(() -> new RuntimeException("Ngày tập không tồn tại"));
        WorkoutPlan plan = day.getWorkoutPlan();

        String warning = computeOrderWarning(user.getId(), plan, day, weekNumber);
        Map<String, String> result = new java.util.HashMap<>();
        result.put("orderWarning", warning);
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

    // ─────────────────────────────────────────────────────────
    // DayMismatch — GIỮ NGUYÊN, độc lập với "orderWarning" mới (mục 14, LOCKED)
    // ─────────────────────────────────────────────────────────
    private record ScheduleCheckInfo(String warning, boolean selectionRequired, List<List<Integer>> options) {}

    private ScheduleCheckInfo buildScheduleCheckInfo(WorkoutSession s) {
        WorkoutPlan plan = s.getWorkoutPlan();
        if (plan == null || s.getSessionDate() == null) return null;

        boolean isAiPlan = Boolean.TRUE.equals(plan.getIsAiGenerated());
        boolean isTemplatePlan = Boolean.FALSE.equals(plan.getIsAiGenerated());
        if (!isAiPlan && !isTemplatePlan) return null;

        List<WorkoutSession> allSessions = sessionRepo.findByPlanOrderBySessionDate(s.getUser().getId(), plan.getId());
        if (allSessions.isEmpty()) return null;

        List<Integer> schedule;

        if (isTemplatePlan) {
            List<WorkoutPlanDay> planDays = plan.getPlanDays() != null
                    ? plan.getPlanDays()
                    : dayRepo.findByWorkoutPlanIdOrderByDayOfWeek(plan.getId());
            if (planDays == null || planDays.isEmpty()) return null;
            schedule = planDays.stream()
                    .map(WorkoutPlanDay::getDayOfWeek)
                    .filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());
            if (schedule.isEmpty()) return null;
        } else {
            if (plan.getConfirmedScheduleDows() != null) {
                schedule = ScheduleCatalog.parse(plan.getConfirmedScheduleDows());
            } else {
                List<List<Integer>> candidates = ScheduleCatalog.candidatesFor(plan.getSessionsPerWeek());
                List<List<Integer>> survivors = eliminateCandidates(candidates, allSessions);

                if (survivors.isEmpty()) {
                    return new ScheduleCheckInfo(
                            "Không thể xác định lịch tập chuẩn từ các buổi tập hiện tại. " +
                                    "Vui lòng chọn một trong các lịch tập khuyến nghị để hệ thống tiếp tục theo dõi chu kỳ tập luyện.",
                            true,
                            candidates);
                } else if (survivors.size() > 1) {
                    return new ScheduleCheckInfo(null, false, null);
                } else {
                    schedule = survivors.get(0);
                }
            }
        }

        int anchorDow = allSessions.get(0).getSessionDate().getDayOfWeek().getValue();
        int idxInSchedule = schedule.indexOf(anchorDow);
        List<Integer> rotated = idxInSchedule >= 0 ? rotate(schedule, idxInSchedule) : schedule;

        int indexOfCurrent = -1;
        for (int i = 0; i < allSessions.size(); i++) {
            if (allSessions.get(i).getId().equals(s.getId())) {
                indexOfCurrent = i;
                break;
            }
        }
        if (indexOfCurrent < 0) return null;

        int expectedDow = rotated.get(indexOfCurrent % rotated.size());
        int actualDow = s.getSessionDate().getDayOfWeek().getValue();

        String warning = null;
        if (actualDow != expectedDow) {
            warning = "⚠️ Theo chu kỳ tập của bạn, buổi này nên rơi vào " + dowVietnameseName(expectedDow)
                    + " nhưng bạn đang tập vào " + dowVietnameseName(actualDow)
                    + ". Tập không đúng chu kỳ có thể làm giáo án không đạt hiệu quả tối ưu.";
        }
        return new ScheduleCheckInfo(warning, false, null);
    }

    private List<List<Integer>> eliminateCandidates(List<List<Integer>> candidates, List<WorkoutSession> allSessions) {
        int anchorDow = allSessions.get(0).getSessionDate().getDayOfWeek().getValue();
        List<List<Integer>> survivors = new ArrayList<>();
        for (List<Integer> candidate : candidates) {
            int idx = candidate.indexOf(anchorDow);
            if (idx < 0) continue;
            List<Integer> rotated = rotate(candidate, idx);
            boolean ok = true;
            for (int i = 0; i < allSessions.size(); i++) {
                int expected = rotated.get(i % rotated.size());
                int actual = allSessions.get(i).getSessionDate().getDayOfWeek().getValue();
                if (expected != actual) { ok = false; break; }
            }
            if (ok) survivors.add(rotated);
        }
        return survivors;
    }

    private List<Integer> rotate(List<Integer> list, int startIdx) {
        List<Integer> rotated = new ArrayList<>();
        int n = list.size();
        for (int i = 0; i < n; i++) rotated.add(list.get((startIdx + i) % n));
        return rotated;
    }

    private String dowVietnameseName(int dow) {
        return switch (dow) {
            case 1 -> "Thứ Hai";
            case 2 -> "Thứ Ba";
            case 3 -> "Thứ Tư";
            case 4 -> "Thứ Năm";
            case 5 -> "Thứ Sáu";
            case 6 -> "Thứ Bảy";
            case 7 -> "Chủ Nhật";
            default -> "?";
        };
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
            note = "Tỉ lệ hoàn thành tuần này của bạn là" + Math.round(avgRate) + "%! Hiệu suất rất cao. Gợi ý tăng tạ khoảng 15%.";
        } else if (avgRate >= 120) {
            note = "Tỉ lệ hoàn thành tuần này của bạn là" + Math.round(avgRate) + "%. Hiệu suất tốt. Gợi ý tăng tạ khoảng 10%.";
        } else if (avgRate >= 90) {
            note = "Tỉ lệ hoàn thành tuần này của bạn là " + Math.round(avgRate) + "%. Hiệu suất ổn định. Gợi ý tăng tạ khoảng 5%.";
        } else if (avgRate >= 80) {
            note = "Tỉ lệ hoàn thành tuần này của bạn là" + Math.round(avgRate) + "%. Giữ nguyên mức tạ hiện tại.";
        } else if (avgRate >= 60) {
            note = "Tỉ lệ hoàn thành tuần này của bạn là" + Math.round(avgRate) + "%. Có thể giảm khoảng 5% để đảm bảo kỹ thuật.";
        } else if (avgRate >= 30) {
            note = "Tỉ lệ hoàn thành tuần này của bạn là" + Math.round(avgRate) + "%. Nên giảm khoảng 10%.";
        } else {
            note = "Tỉ lệ hoàn thành tuần này của bạn là" + Math.round(avgRate) + "%. Nên giảm khoảng 20%.";
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
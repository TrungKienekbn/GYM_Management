package com.example.gymmanagement.service.plan;

import com.example.gymmanagement.enums.FitnessLevel;
import com.example.gymmanagement.enums.Goal;
import com.example.gymmanagement.enums.MuscleGroup;

import java.util.*;

/**
 * Sinh "nhóm cơ theo từng buổi trong tuần" (dayIndex, KHÔNG phải dayOfWeek) và "số bài
 * tập/nhóm cơ/buổi" theo bảng tra cứu tường minh (mục 6.1.1 → 6.1.3, 6.1.5 — đã bỏ 6.1.4
 * FLEXIBILITY vì Goal FLEXIBILITY không còn tồn tại).
 *
 * dayIndex (0,1,2,...) là thứ tự buổi tập trong tuần, độc lập với dayOfWeek thực tế
 * (dayOfWeek đến từ ScheduleCatalog và được WorkoutPlanService ánh xạ theo đúng thứ tự
 * dayIndex -> vị trí trong candidate lịch).
 *
 * ────────────────────────────────────────────────────────────────
 * THUẬT TOÁN
 * ────────────────────────────────────────────────────────────────
 * 1) Với mỗi nhóm cơ xuất hiện trong tuần, xác định f = số buổi (ngày) nhóm cơ đó
 *    THỰC SỰ xuất hiện (đếm theo bảng DAY_MUSCLE_GROUPS).
 * 2) BaseQuota = số bài/nhóm cơ/TUẦN theo (Goal, FitnessLevel) — bảng BASE_QUOTA.
 * 3) AdjustedQuota:
 *      f >= 2  -> AdjustedQuota = BaseQuota
 *      f == 1  -> AdjustedQuota = min(BaseQuota, T_max=4)
 * 4) Chia AdjustedQuota cho f buổi bằng Largest Remainder Method (mọi buổi trọng số
 *    bằng nhau): base = AdjustedQuota / f, remainder = AdjustedQuota % f; các buổi đầu
 *    tiên (theo thứ tự dayIndex xuất hiện) nhận thêm 1 cho tới khi hết remainder.
 * 5) MỚI — Business Rule: mỗi ngày không được vượt quá MAX_EXERCISES_PER_GROUP_PER_DAY
 *    bài/nhóm cơ. Nếu một ngày vượt ngưỡng sau bước 4, phần dư (overflow) được ưu tiên
 *    chuyển sang các ngày khác CÙNG nhóm cơ đó đang có ÍT bài nhất trước (không phải
 *    theo thứ tự dayIndex). Nếu mọi ngày đã đạt ngưỡng thì overflow bị bỏ. Không tạo
 *    ngày mới, không thêm nhóm cơ vào ngày vốn không có, không đổi split/frequency/LRM.
 */
public final class MuscleGroupSplitPlanner {

    private MuscleGroupSplitPlanner() {}

    private static final int T_MAX = 4;

    // ── Business Rule: giới hạn cứng số bài/1 nhóm cơ/1 ngày. Không hardcode số 3
    // rải rác trong thuật toán — chỉ khai báo duy nhất ở đây. ──
    private static final int MAX_EXERCISES_PER_GROUP_PER_DAY = 3;

    // ── Nhóm cơ theo từng buổi trong tuần, tra theo (Goal, sessionsPerWeek) ──
    // Mỗi phần tử ngoài cùng = 1 buổi (dayIndex theo thứ tự), giá trị = các nhóm cơ của buổi đó.
    // ĐÃ XOÁ: entry Goal.FLEXIBILITY.
    private static final Map<Goal, Map<Integer, List<List<MuscleGroup>>>> DAY_MUSCLE_GROUPS = new EnumMap<>(Goal.class);
    static {
        // ── MUSCLE_GAIN ──
        Map<Integer, List<List<MuscleGroup>>> muscleGain = new HashMap<>();
        muscleGain.put(4, List.of(
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS)
        ));
        muscleGain.put(5, List.of(
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS)
        ));
        muscleGain.put(6, List.of(
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO)
        ));
        DAY_MUSCLE_GROUPS.put(Goal.MUSCLE_GAIN, muscleGain);

        // ── WEIGHT_LOSS ──
        Map<Integer, List<List<MuscleGroup>>> weightLoss = new HashMap<>();
        weightLoss.put(4, List.of(
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS)
        ));
        weightLoss.put(5, List.of(
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO)
        ));
        weightLoss.put(6, List.of(
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO)
        ));
        DAY_MUSCLE_GROUPS.put(Goal.WEIGHT_LOSS, weightLoss);

        // ── ENDURANCE ──
        Map<Integer, List<List<MuscleGroup>>> endurance = new HashMap<>();
        endurance.put(2, List.of(
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO)
        ));
        endurance.put(3, List.of(
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO)
        ));
        endurance.put(4, List.of(
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO)
        ));
        DAY_MUSCLE_GROUPS.put(Goal.ENDURANCE, endurance);

        // ── MAINTENANCE ──
        Map<Integer, List<List<MuscleGroup>>> maintenance = new HashMap<>();
        maintenance.put(3, List.of(
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO)
        ));
        maintenance.put(4, List.of(
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS)
        ));
        maintenance.put(5, List.of(
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS),
                List.of(MuscleGroup.FULL_BODY, MuscleGroup.CARDIO),
                List.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
                List.of(MuscleGroup.BACK, MuscleGroup.CORE, MuscleGroup.LEGS)
        ));
        DAY_MUSCLE_GROUPS.put(Goal.MAINTENANCE, maintenance);
    }

    // ── BaseQuota: số bài/nhóm cơ/TUẦN theo (Goal, FitnessLevel) ──
    // ĐÃ XOÁ: entry Goal.FLEXIBILITY.
    private static final Map<Goal, Map<FitnessLevel, Integer>> BASE_QUOTA = new EnumMap<>(Goal.class);
    static {
        Map<FitnessLevel, Integer> strengthLike = new EnumMap<>(FitnessLevel.class);
        strengthLike.put(FitnessLevel.BEGINNER, 4);
        strengthLike.put(FitnessLevel.INTERMEDIATE, 4);
        strengthLike.put(FitnessLevel.ADVANCED, 6);
        BASE_QUOTA.put(Goal.MUSCLE_GAIN, strengthLike);
        BASE_QUOTA.put(Goal.WEIGHT_LOSS, new EnumMap<>(strengthLike));

        // ── ENDURANCE: dùng đúng số liệu FLEXIBILITY cũ (4/4/6) — chủ đích của bạn ──
        Map<FitnessLevel, Integer> enduranceQuota = new EnumMap<>(FitnessLevel.class);
        enduranceQuota.put(FitnessLevel.BEGINNER, 4);
        enduranceQuota.put(FitnessLevel.INTERMEDIATE, 4);
        enduranceQuota.put(FitnessLevel.ADVANCED, 6);
        BASE_QUOTA.put(Goal.ENDURANCE, enduranceQuota);

        // ── MAINTENANCE: GIỮ NGUYÊN số liệu gốc (3/4/5) — KHÔNG liên quan đến việc đổi ENDURANCE ──
        Map<FitnessLevel, Integer> maintenanceQuota = new EnumMap<>(FitnessLevel.class);
        maintenanceQuota.put(FitnessLevel.BEGINNER, 3);
        maintenanceQuota.put(FitnessLevel.INTERMEDIATE, 4);
        maintenanceQuota.put(FitnessLevel.ADVANCED, 5);
        BASE_QUOTA.put(Goal.MAINTENANCE, maintenanceQuota);
    }

    /**
     * Trả về, cho từng buổi trong tuần (index = dayIndex, 0..sessions-1), map
     * "nhóm cơ -> số bài tập cần chọn cho nhóm cơ đó trong buổi này".
     * Thứ tự các entry trong mỗi Map giữ đúng thứ tự xuất hiện của nhóm cơ trong bảng.
     */
    public static List<Map<MuscleGroup, Integer>> buildWeekPlan(Goal goal, FitnessLevel level, int sessions) {
        List<List<MuscleGroup>> dayGroups = dayGroupsFor(goal, sessions);
        return buildWeekPlan(goal, level, sessions, dayGroups);
    }

    public static List<Map<MuscleGroup, Integer>> buildWeekPlan(Goal goal, FitnessLevel level, int sessions,
                                                                 List<List<MuscleGroup>> dayGroups) {
        if (dayGroups == null || dayGroups.size() != sessions) {
            throw new IllegalArgumentException("Cấu hình nhóm cơ không khớp số buổi/tuần.");
        }
        int baseQuota = baseQuotaFor(goal, level);

        // group -> danh sách dayIndex mà nhóm cơ đó xuất hiện (theo thứ tự tăng dần)
        Map<MuscleGroup, List<Integer>> occurrenceDays = new EnumMap<>(MuscleGroup.class);
        for (int d = 0; d < dayGroups.size(); d++) {
            for (MuscleGroup mg : dayGroups.get(d)) {
                occurrenceDays.computeIfAbsent(mg, k -> new ArrayList<>()).add(d);
            }
        }

        // group -> (dayIndex -> số bài của nhóm cơ đó trong buổi dayIndex)
        Map<MuscleGroup, Map<Integer, Integer>> perGroupPerDay = new EnumMap<>(MuscleGroup.class);
        for (Map.Entry<MuscleGroup, List<Integer>> e : occurrenceDays.entrySet()) {
            List<Integer> days = e.getValue();
            int f = days.size();
            int adjustedQuota = (f == 1) ? Math.min(baseQuota, T_MAX) : baseQuota;
            Map<Integer, Integer> distributed = largestRemainderDistribute(adjustedQuota, days);
            // ── MỚI: cap về MAX_EXERCISES_PER_GROUP_PER_DAY, ưu tiên chuyển bài dư sang
            // ngày khác cùng nhóm cơ đang có ít bài nhất trước khi bỏ hẳn ──
            perGroupPerDay.put(e.getKey(), capAndRedistribute(distributed, days));
        }

        List<Map<MuscleGroup, Integer>> result = new ArrayList<>();
        for (int d = 0; d < dayGroups.size(); d++) {
            Map<MuscleGroup, Integer> dayMap = new LinkedHashMap<>();
            for (MuscleGroup mg : dayGroups.get(d)) {
                dayMap.put(mg, perGroupPerDay.get(mg).get(d));
            }
            result.add(dayMap);
        }
        return result;
    }

    /** Largest Remainder Method: chia quota cho các dayIndex trong "days", trọng số bằng nhau. */
    private static Map<Integer, Integer> largestRemainderDistribute(int quota, List<Integer> days) {
        int f = days.size();
        int base = quota / f;
        int remainder = quota % f;
        Map<Integer, Integer> dist = new LinkedHashMap<>();
        for (int i = 0; i < f; i++) {
            int count = base + (i < remainder ? 1 : 0);
            dist.put(days.get(i), count);
        }
        return dist;
    }

    /**
     * Ép mỗi ngày trong "days" không vượt quá MAX_EXERCISES_PER_GROUP_PER_DAY.
     * Overflow được ưu tiên chuyển sang ngày ĐANG CÓ ÍT BÀI NHẤT trước (không phải
     * theo thứ tự dayIndex). Nếu nhiều ngày cùng mức thấp nhất, giữ nguyên thứ tự
     * xuất hiện ban đầu trong "days" (stable sort). Chỉ khi mọi ngày đã chạm ngưỡng
     * thì overflow mới bị bỏ. Không tạo ngày mới, không thêm nhóm cơ vào ngày vốn
     * không có (chỉ thao tác trong "days").
     */
    private static Map<Integer, Integer> capAndRedistribute(Map<Integer, Integer> dist, List<Integer> days) {
        Map<Integer, Integer> result = new LinkedHashMap<>(dist);

        int overflow = 0;
        for (Integer d : days) {
            int q = result.get(d);
            if (q > MAX_EXERCISES_PER_GROUP_PER_DAY) {
                overflow += q - MAX_EXERCISES_PER_GROUP_PER_DAY;
                result.put(d, MAX_EXERCISES_PER_GROUP_PER_DAY);
            }
        }

        if (overflow > 0) {
            // Sắp xếp các ngày theo quota hiện tại TĂNG DẦN (ít bài nhất ưu tiên nhận trước).
            // List.sort là stable -> giữ nguyên thứ tự gốc khi quota bằng nhau.
            List<Integer> byAscendingQuota = new ArrayList<>(days);
            byAscendingQuota.sort(Comparator.comparingInt(result::get));

            for (Integer d : byAscendingQuota) {
                if (overflow <= 0) break;
                int q = result.get(d);
                int capacity = MAX_EXERCISES_PER_GROUP_PER_DAY - q;
                if (capacity > 0) {
                    int transfer = Math.min(capacity, overflow);
                    result.put(d, q + transfer);
                    overflow -= transfer;
                }
            }
        }
        // overflow còn lại (nếu có) bị bỏ — không tạo ngày mới, không random ở bước này
        return result;
    }

    private static List<List<MuscleGroup>> dayGroupsFor(Goal goal, int sessions) {
        Map<Integer, List<List<MuscleGroup>>> byGoal = DAY_MUSCLE_GROUPS.get(goal);
        if (byGoal == null || !byGoal.containsKey(sessions)) {
            throw new IllegalStateException("Chưa khai báo nhóm cơ theo ngày cho Goal=" + goal
                    + ", sessions=" + sessions + " — thêm vào MuscleGroupSplitPlanner.DAY_MUSCLE_GROUPS.");
        }
        return byGoal.get(sessions);
    }

    public static List<List<MuscleGroup>> defaultDayGroupsFor(Goal goal, int sessions) {
        return dayGroupsFor(goal, sessions);
    }

    private static int baseQuotaFor(Goal goal, FitnessLevel level) {
        Map<FitnessLevel, Integer> byLevel = BASE_QUOTA.get(goal);
        if (byLevel == null || !byLevel.containsKey(level)) {
            throw new IllegalStateException("Chưa khai báo BaseQuota cho Goal=" + goal
                    + ", Level=" + level + " — thêm vào MuscleGroupSplitPlanner.BASE_QUOTA.");
        }
        return byLevel.get(level);
    }
}

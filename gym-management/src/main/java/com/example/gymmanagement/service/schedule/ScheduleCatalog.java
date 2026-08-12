package com.example.gymmanagement.service.schedule;

import java.util.*;

/**
 * Danh mục lịch tập khuyến nghị theo số buổi/tuần.
 * Mỗi sessionsPerWeek chỉ có ĐÚNG 1 lịch khuyến nghị duy nhất (không còn nhiều lựa chọn
 * Lịch A/B...). Dùng cho: sinh giáo án AI (buildPlanDaysNew), hiển thị Suggested Days,
 * và cảnh báo Check-in sai lịch (computeScheduleWarning) — KHÔNG còn phục vụ Confirm
 * Schedule/DayMismatch elimination (đã xoá khỏi hệ thống).
 */
public final class ScheduleCatalog {

    private ScheduleCatalog() {}

    // 1=Monday ... 7=Sunday (ISO dayOfWeek)
    private static final Map<Integer, List<Integer>> RECOMMENDED = new HashMap<>();
    static {
        RECOMMENDED.put(1, List.of(1));
        RECOMMENDED.put(2, List.of(1, 4));            // Monday - Thursday
        RECOMMENDED.put(3, List.of(1, 3, 5));         // Monday-Wednesday-Friday
        RECOMMENDED.put(4, List.of(1, 2, 4, 5));      // Monday-Tuesday-Thursday-Friday
        RECOMMENDED.put(5, List.of(1, 2, 3, 5, 6));   // Monday-Tuesday-Wednesday-Friday-Saturday
        RECOMMENDED.put(6, List.of(1, 2, 3, 4, 5, 6));// Monday-Tuesday-Wednesday-Thursday-Friday-Saturday
        RECOMMENDED.put(7, List.of(1, 2, 3, 4, 5, 6, 7));
    }

    /** Trả về lịch khuyến nghị DUY NHẤT cho 1 số buổi/tuần cho trước. */
    public static List<Integer> recommendedFor(int sessions) {
        List<Integer> result = RECOMMENDED.get(sessions);
        if (result == null) {
            throw new IllegalStateException("Chưa khai báo lịch tập cho sessionsPerWeek=" + sessions
                    + " — thêm vào ScheduleCatalog.RECOMMENDED trước khi dùng.");
        }
        return result;
    }
}

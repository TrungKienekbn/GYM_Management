package com.example.gymmanagement.shift;

import com.example.gymmanagement.entity.User;
import com.example.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkShiftService {
    private final WorkShiftRepository shifts;
    private final UserRepository users;

    @Transactional
    public Map<String, Object> assign(Long staffUserId, LocalDate date, LocalTime start, LocalTime end) {
        User staff = users.findById(staffUserId).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        if (end.isBefore(start)) throw new RuntimeException("Giờ kết thúc phải sau giờ bắt đầu");
        WorkShift s = WorkShift.builder().user(staff).shiftDate(date).plannedStart(start).plannedEnd(end).build();
        return map(shifts.save(s));
    }

    public List<Map<String, Object>> allShifts() {
        return shifts.findAllByOrderByShiftDateDesc().stream().map(this::map).toList();
    }

    public List<Map<String, Object>> myShifts(String email) {
        return shifts.findByUserIdOrderByShiftDateDesc(user(email).getId()).stream().map(this::map).toList();
    }

    @Transactional
    public Map<String, Object> checkIn(String email, Long shiftId) {
        WorkShift s = owned(email, shiftId);
        if (s.getCheckInAt() != null) throw new RuntimeException("Ca này đã check-in rồi");
        s.setCheckInAt(LocalDateTime.now());
        s.setStatus(ShiftStatus.CHECKED_IN);
        return map(shifts.save(s));
    }

    @Transactional
    public Map<String, Object> checkOut(String email, Long shiftId, String handoverNote) {
        WorkShift s = owned(email, shiftId);
        if (s.getCheckInAt() == null) throw new RuntimeException("Chưa check-in thì không thể check-out");
        if (s.getCheckOutAt() != null) throw new RuntimeException("Ca này đã check-out rồi");
        s.setCheckOutAt(LocalDateTime.now());
        s.setStatus(ShiftStatus.COMPLETED);
        s.setHandoverNote(handoverNote);
        return map(shifts.save(s));
    }

    private WorkShift owned(String email, Long id) {
        User u = user(email);
        WorkShift s = shifts.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc"));
        boolean isAdmin = "ROLE_ADMIN".equals(u.getRole().getRoleName());
        if (!isAdmin && !s.getUser().getId().equals(u.getId())) throw new RuntimeException("Không có quyền thao tác ca này");
        return s;
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private Map<String, Object> map(WorkShift s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("userId", s.getUser().getId());
        m.put("userName", s.getUser().getFullName());
        m.put("shiftDate", s.getShiftDate());
        m.put("plannedStart", s.getPlannedStart());
        m.put("plannedEnd", s.getPlannedEnd());
        m.put("status", s.getStatus());
        m.put("checkInAt", s.getCheckInAt());
        m.put("checkOutAt", s.getCheckOutAt());
        m.put("handoverNote", s.getHandoverNote());
        return m;
    }
}
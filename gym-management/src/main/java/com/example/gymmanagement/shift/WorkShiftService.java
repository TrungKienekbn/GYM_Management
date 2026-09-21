package com.example.gymmanagement.shift;

import com.example.gymmanagement.entity.User;
import com.example.gymmanagement.repository.UserRepository;
import com.example.gymmanagement.shop.SalesChannel;
import com.example.gymmanagement.shop.StoreOrder;
import com.example.gymmanagement.shop.StoreOrderRepository;
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
    private final StoreOrderRepository orders;

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
    public Map<String, Object> checkIn(String email, Long shiftId, Double cashAtStart) {
        WorkShift s = owned(email, shiftId);
        if (cashAtStart != null && (!Double.isFinite(cashAtStart) || cashAtStart < 0)) throw new RuntimeException("Tiền đầu ca không hợp lệ");
        if (s.getCheckInAt() != null) throw new RuntimeException("Ca này đã check-in rồi");
        s.setCheckInAt(LocalDateTime.now());
        s.setStatus(ShiftStatus.CHECKED_IN);
        s.setCashAtStart(cashAtStart == null ? 0d : cashAtStart);
        return map(shifts.save(s));
    }

    @Transactional
    public Map<String, Object> checkOut(String email, Long shiftId, Double cashCounted, String handoverNote) {
        WorkShift s = owned(email, shiftId);
        if (cashCounted == null || !Double.isFinite(cashCounted) || cashCounted < 0) throw new RuntimeException("Vui lòng nhập tiền kiểm đếm cuối ca hợp lệ");
        if (s.getCheckInAt() == null) throw new RuntimeException("Chưa check-in thì không thể check-out");
        if (s.getCheckOutAt() != null) throw new RuntimeException("Ca này đã check-out rồi");

        LocalDateTime now = LocalDateTime.now();
        List<StoreOrder> posOrders = orders.findByCreatedByStaffIdAndChannelAndCreatedAtBetween(
                s.getUser().getId(), SalesChannel.POS, s.getCheckInAt(), now);

        double cashRevenue = posOrders.stream()
                .filter(o -> "CASH".equals(o.getPaymentMethod()))
                .mapToDouble(o -> o.getTotal() == null ? 0 : o.getTotal()).sum();
        double bankRevenue = posOrders.stream()
                .filter(o -> "BANK_TRANSFER".equals(o.getPaymentMethod()))
                .mapToDouble(o -> o.getTotal() == null ? 0 : o.getTotal()).sum();

        double cashAtStart = s.getCashAtStart() == null ? 0 : s.getCashAtStart();
        double expected = cashAtStart + cashRevenue;
        double counted = cashCounted == null ? 0 : cashCounted;

        s.setCheckOutAt(now);
        s.setStatus(ShiftStatus.COMPLETED);
        s.setPosCashRevenue(cashRevenue);
        s.setPosBankRevenue(bankRevenue);
        s.setExpectedCash(expected);
        s.setCashCounted(counted);
        s.setCashDifference(counted - expected);
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
        m.put("cashAtStart", s.getCashAtStart());
        m.put("cashCounted", s.getCashCounted());
        m.put("posCashRevenue", s.getPosCashRevenue());
        m.put("posBankRevenue", s.getPosBankRevenue());
        m.put("expectedCash", s.getExpectedCash());
        m.put("cashDifference", s.getCashDifference());
        m.put("handoverNote", s.getHandoverNote());
        return m;
    }
}

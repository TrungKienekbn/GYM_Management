package com.example.gymmanagement.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository vouchers;

    @Transactional
    public Voucher save(Long id, Voucher input) {
        Voucher v = id == null ? new Voucher() : vouchers.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        if (input.getCode() == null || input.getCode().isBlank()) throw new RuntimeException("Mã voucher không được để trống");
        if (input.getValue() == null || input.getValue() <= 0) throw new RuntimeException("Giá trị giảm không hợp lệ");
        if (input.getType() == VoucherType.PERCENT && input.getValue() > 100) throw new RuntimeException("Phần trăm giảm tối đa là 100");
        v.setCode(input.getCode().trim().toUpperCase());
        v.setDescription(input.getDescription());
        v.setType(input.getType());
        v.setValue(input.getValue());
        v.setMinOrderAmount(input.getMinOrderAmount());
        v.setMaxDiscountAmount(input.getMaxDiscountAmount());
        v.setUsageLimit(input.getUsageLimit());
        v.setStartAt(input.getStartAt());
        v.setEndAt(input.getEndAt());
        v.setActive(input.getActive() == null ? true : input.getActive());
        return vouchers.save(v);
    }

    public List<Voucher> all() { return vouchers.findAllByOrderByCreatedAtDesc(); }

    @Transactional
    public void remove(Long id) {
        Voucher v = vouchers.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        v.setActive(false);
        vouchers.save(v);
    }

    public Map.Entry<Voucher, Double> validate(String code, double subtotal) {
        if (code == null || code.isBlank()) throw new RuntimeException("Vui lòng nhập mã voucher");
        Voucher v = vouchers.findByCodeIgnoreCase(code.trim()).orElseThrow(() -> new RuntimeException("Mã voucher không tồn tại"));
        if (!Boolean.TRUE.equals(v.getActive())) throw new RuntimeException("Voucher đã ngừng áp dụng");
        LocalDateTime now = LocalDateTime.now();
        if (v.getStartAt() != null && now.isBefore(v.getStartAt())) throw new RuntimeException("Voucher chưa tới thời gian áp dụng");
        if (v.getEndAt() != null && now.isAfter(v.getEndAt())) throw new RuntimeException("Voucher đã hết hạn");
        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) throw new RuntimeException("Voucher đã hết lượt sử dụng");
        if (v.getMinOrderAmount() != null && subtotal < v.getMinOrderAmount()) throw new RuntimeException("Đơn hàng cần tối thiểu " + Math.round(v.getMinOrderAmount()) + "đ để áp dụng voucher này");

        double discount = v.getType() == VoucherType.PERCENT ? subtotal * v.getValue() / 100 : v.getValue();
        if (v.getMaxDiscountAmount() != null) discount = Math.min(discount, v.getMaxDiscountAmount());
        discount = Math.min(discount, subtotal);
        return Map.entry(v, discount);
    }

    @Transactional
    public void markUsed(Voucher v) {
        v.setUsedCount((v.getUsedCount() == null ? 0 : v.getUsedCount()) + 1);
        vouchers.save(v);
    }

    public List<Map<String, Object>> publicList() {
        LocalDateTime now = LocalDateTime.now();
        return vouchers.findAllByOrderByCreatedAtDesc().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .filter(v -> v.getStartAt() == null || !now.isBefore(v.getStartAt()))
                .filter(v -> v.getEndAt() == null || !now.isAfter(v.getEndAt()))
                .filter(v -> v.getUsageLimit() == null || v.getUsedCount() < v.getUsageLimit())
                .map(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("code", v.getCode()); m.put("description", v.getDescription());
                    m.put("type", v.getType()); m.put("value", v.getValue());
                    m.put("minOrderAmount", v.getMinOrderAmount()); m.put("endAt", v.getEndAt());
                    return m;
                }).toList();
    }
}
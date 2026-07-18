package com.example.gymmanagement.dto.request;

import com.example.gymmanagement.enums.AssessmentMetricType;
import lombok.Data;
import java.util.List;

@Data
public class CheckOutRequest {
    private Double checkoutWeight;
    private Double checkoutBodyFat;
    private String notes;
    private List<ExerciseLogRequest> exerciseLogs;

    // ── MỚI (Patch 10): dữ liệu Assessment nhập ở Popup Review — chỉ có giá trị
    // khi Goal = ENDURANCE. Gửi kèm ở LẦN GỌI THỨ HAI của Checkout. ──
    private AssessmentMetricType assessmentMetricType;
    private Integer assessmentValue;
}
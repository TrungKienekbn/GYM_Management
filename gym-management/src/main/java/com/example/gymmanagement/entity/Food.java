package com.example.gymmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "foods")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer calories;      // kcal
    private Double proteinGrams;   // g
    private Double fatGrams;       // g
    private Double weightGrams;    // khối lượng khẩu phần món ăn này, tính bằng gram — dùng để quy đổi "trên mỗi kg"

    // Tự tính "trên mỗi 1kg" từ số liệu khẩu phần — không lưu DB, chỉ trả JSON cho FE.
    @Transient
    public Double getCaloriesPerKg() {
        if (weightGrams == null || weightGrams <= 0 || calories == null) return null;
        return Math.round((calories / weightGrams) * 1000 * 100) / 100.0;
    }
    @Transient
    public Double getProteinPerKg() {
        if (weightGrams == null || weightGrams <= 0 || proteinGrams == null) return null;
        return Math.round((proteinGrams / weightGrams) * 1000 * 100) / 100.0;
    }
    @Transient
    public Double getFatPerKg() {
        if (weightGrams == null || weightGrams <= 0 || fatGrams == null) return null;
        return Math.round((fatGrams / weightGrams) * 1000 * 100) / 100.0;
    }

    @Lob
    private String ingredients;    // nguyên liệu nấu

    @Lob
    private String instructions;   // cách nấu

    private String imageUrl;       // ảnh minh họa (tùy chọn)

    // Lưu dạng chuỗi phân tách dấu phẩy, VD: "WEIGHT_LOSS,MAINTENANCE"
    // vì 1 món ăn có thể phù hợp cùng lúc nhiều mục tiêu (giảm cân / tăng cân / duy trì)
    private String suitableGoals;

    @Builder.Default
    private Boolean isActive = true;

    // Trả kèm dạng danh sách cho FE dễ dùng (checkbox/tag), không lưu xuống DB
    @Transient
    public List<String> getSuitableGoalsList() {
        List<String> result = new ArrayList<>();
        if (suitableGoals == null || suitableGoals.isBlank()) return result;
        for (String g : suitableGoals.split(",")) {
            if (!g.isBlank()) result.add(g.trim());
        }
        return result;
    }
}
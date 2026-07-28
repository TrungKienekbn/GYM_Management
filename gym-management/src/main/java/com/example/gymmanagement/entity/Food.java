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
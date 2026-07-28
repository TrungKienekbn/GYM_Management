package com.example.gymmanagement.dto.request;

import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class FoodRequest {
    private String name;
    private Integer calories;
    private Double proteinGrams;
    private Double fatGrams;
    private String ingredients;
    private String instructions;
    private String imageUrl;

    // ["WEIGHT_LOSS","MUSCLE_GAIN","MAINTENANCE"]
    private List<String> suitableGoals;
}
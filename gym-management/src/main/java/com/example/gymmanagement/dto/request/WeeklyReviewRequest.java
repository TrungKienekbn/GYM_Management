package com.example.gymmanagement.dto.request;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class WeeklyReviewRequest {
    private Long planId;
    private Integer weekNumber;
    private Integer rating;
    private String comment;
}
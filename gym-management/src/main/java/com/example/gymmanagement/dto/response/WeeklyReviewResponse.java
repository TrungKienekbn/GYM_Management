package com.example.gymmanagement.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyReviewResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long planId;
    private String planName;
    private Integer weekNumber;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
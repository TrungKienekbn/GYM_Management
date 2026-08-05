package com.example.gymmanagement.dto.request;

import lombok.Data;

@Data
public class SupportRatingRequest {
    private Integer rating;
    private String comment;
}

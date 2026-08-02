package com.example.gymmanagement.dto.request;

import lombok.Data;

@Data
public class PhoneLast4LoginRequest {
    private String email;
    private String lastFourDigits;
}

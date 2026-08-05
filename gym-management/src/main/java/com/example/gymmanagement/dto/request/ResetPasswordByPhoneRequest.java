package com.example.gymmanagement.dto.request;

import lombok.Data;

@Data
public class ResetPasswordByPhoneRequest {
    private String email;
    private String lastFourDigits;
    private String newPassword;
}

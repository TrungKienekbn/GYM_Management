package com.example.gymmanagement.pet;

import lombok.*;

@Data @Builder
public class CosmeticItemResponse {
    private String code;
    private CosmeticSlot slot;
    private String displayName;
    private String colorHex;
    private long price;
    private boolean free;
    private boolean owned;
    private boolean equipped;
}
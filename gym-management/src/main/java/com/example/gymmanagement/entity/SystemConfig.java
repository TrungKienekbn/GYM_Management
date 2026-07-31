package com.example.gymmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemConfig {

    @Id
    private String configKey;      // VD: "MANA_REGEN_RATE_1_DAY"

    private Double configValue;    // VD: 0.75

    @Lob
    private String description;    // Giải thích công thức — hiện khi hover dấu (?)

    private String category;       // Nhóm hiển thị: "Fitness Score", "Mana", "Giáo án"...
}
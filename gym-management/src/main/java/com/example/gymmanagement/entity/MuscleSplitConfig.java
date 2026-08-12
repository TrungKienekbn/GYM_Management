package com.example.gymmanagement.entity;
import com.example.gymmanagement.enums.Goal;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="muscle_split_configs", uniqueConstraints=@UniqueConstraint(columnNames={"goal","sessions_per_week"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MuscleSplitConfig {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Goal goal;
 @Column(name="sessions_per_week",nullable=false) private Integer sessionsPerWeek;
 @Column(name="day_groups",nullable=false,length=1000) private String dayGroups;
}

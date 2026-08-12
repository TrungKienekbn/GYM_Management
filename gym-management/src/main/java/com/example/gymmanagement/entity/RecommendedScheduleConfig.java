package com.example.gymmanagement.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="recommended_schedule_configs", uniqueConstraints=@UniqueConstraint(columnNames="sessions_per_week"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendedScheduleConfig {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="sessions_per_week",nullable=false) private Integer sessionsPerWeek;
 @Column(name="recommended_days",nullable=false,length=30) private String recommendedDays;
}

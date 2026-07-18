package com.example.gymmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","workout_plan_id","week_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id")
    private WorkoutPlan workoutPlan;

    private Integer weekNumber;
    private Integer rating;      // 1-5 sao
    private String  comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
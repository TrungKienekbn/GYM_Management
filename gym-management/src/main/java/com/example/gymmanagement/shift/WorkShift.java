package com.example.gymmanagement.shift;
import com.example.gymmanagement.entity.User;
import jakarta.persistence.*; import lombok.*;
import java.time.*;

@Entity @Table(name="work_shifts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkShift {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private User user;
    private LocalDate shiftDate;
    private LocalTime plannedStart;
    private LocalTime plannedEnd;
    @Enumerated(EnumType.STRING) @Builder.Default private ShiftStatus status = ShiftStatus.SCHEDULED;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private Double cashAtStart;
    private Double cashCounted;
    private Double posCashRevenue;
    private Double posBankRevenue;
    private Double expectedCash;
    private Double cashDifference;
    @Column(length=2000) private String handoverNote;
    private LocalDateTime createdAt;
    @PrePersist void init(){ if(createdAt==null) createdAt=LocalDateTime.now(); }
}
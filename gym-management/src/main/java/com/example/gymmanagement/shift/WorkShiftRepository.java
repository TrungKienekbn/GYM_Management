package com.example.gymmanagement.shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    List<WorkShift> findByUserIdOrderByShiftDateDesc(Long userId);
    List<WorkShift> findAllByOrderByShiftDateDesc();
}
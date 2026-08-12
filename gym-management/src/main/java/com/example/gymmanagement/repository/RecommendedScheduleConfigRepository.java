package com.example.gymmanagement.repository;
import com.example.gymmanagement.entity.RecommendedScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RecommendedScheduleConfigRepository extends JpaRepository<RecommendedScheduleConfig,Long> {
 Optional<RecommendedScheduleConfig> findBySessionsPerWeek(Integer sessionsPerWeek);
}

package com.example.gymmanagement.repository;
import com.example.gymmanagement.entity.MuscleSplitConfig;
import com.example.gymmanagement.enums.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MuscleSplitConfigRepository extends JpaRepository<MuscleSplitConfig,Long> {
 Optional<MuscleSplitConfig> findByGoalAndSessionsPerWeek(Goal goal,Integer sessionsPerWeek);
}

package com.example.gymmanagement.repository;

import com.example.gymmanagement.entity.InjuryAreaOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InjuryAreaOptionRepository extends JpaRepository<InjuryAreaOption, Long> {
    List<InjuryAreaOption> findAllByOrderByLabelAsc();
    boolean existsByLabelIgnoreCase(String label);
    boolean existsByCodeIgnoreCase(String code);
}

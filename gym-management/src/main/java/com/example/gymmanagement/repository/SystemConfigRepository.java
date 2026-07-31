package com.example.gymmanagement.repository;

import com.example.gymmanagement.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    List<SystemConfig> findAllByOrderByCategoryAscConfigKeyAsc();
}
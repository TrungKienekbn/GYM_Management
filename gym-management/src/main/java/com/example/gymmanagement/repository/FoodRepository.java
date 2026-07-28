package com.example.gymmanagement.repository;

import com.example.gymmanagement.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findByIsActiveTrueOrderByIdDesc();

    List<Food> findByIsActiveTrueAndNameContainingIgnoreCaseOrderByIdDesc(String keyword);
}
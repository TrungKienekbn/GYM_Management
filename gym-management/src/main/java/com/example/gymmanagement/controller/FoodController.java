package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.request.FoodRequest;
import com.example.gymmanagement.dto.response.ApiResponse;
import com.example.gymmanagement.entity.Food;
import com.example.gymmanagement.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodRepository foodRepository;

    // Dùng chung cho Admin (không truyền goal) và User (truyền goal để lọc theo mục tiêu)
    // VD: GET /api/foods?goal=WEIGHT_LOSS&keyword=ga
    @GetMapping
    public ResponseEntity<ApiResponse<List<Food>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String goal) {

        List<Food> list = (keyword == null || keyword.isBlank())
                ? foodRepository.findByIsActiveTrueOrderByIdDesc()
                : foodRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrderByIdDesc(keyword.trim());

        if (goal != null && !goal.isBlank()) {
            String g = goal.trim().toUpperCase();
            list = list.stream()
                    .filter(f -> f.getSuitableGoals() != null && f.getSuitableGoals().toUpperCase().contains(g))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Food>> getById(@PathVariable Long id) {
        Food f = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
        return ResponseEntity.ok(ApiResponse.success(f));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Food>> create(@RequestBody FoodRequest req) {
        Food food = Food.builder()
                .name(req.getName())
                .calories(req.getCalories())
                .proteinGrams(req.getProteinGrams())
                .fatGrams(req.getFatGrams())
                .ingredients(req.getIngredients())
                .instructions(req.getInstructions())
                .imageUrl(req.getImageUrl())
                .suitableGoals(joinGoals(req.getSuitableGoals()))
                .isActive(true)
                .weightGrams(req.getWeightGrams())
                .build();
        return ResponseEntity.ok(ApiResponse.success(foodRepository.save(food), "Đã thêm món ăn!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Food>> update(@PathVariable Long id, @RequestBody FoodRequest req) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
        if (req.getName()          != null) food.setName(req.getName());
        if (req.getCalories()      != null) food.setCalories(req.getCalories());
        if (req.getProteinGrams()  != null) food.setProteinGrams(req.getProteinGrams());
        if (req.getFatGrams()      != null) food.setFatGrams(req.getFatGrams());
        if (req.getIngredients()   != null) food.setIngredients(req.getIngredients());
        if (req.getInstructions()  != null) food.setInstructions(req.getInstructions());
        if (req.getImageUrl()      != null) food.setImageUrl(req.getImageUrl());
        if (req.getSuitableGoals() != null) food.setSuitableGoals(joinGoals(req.getSuitableGoals()));
        if (req.getWeightGrams() != null) food.setWeightGrams(req.getWeightGrams());
        return ResponseEntity.ok(ApiResponse.success(foodRepository.save(food), "Đã cập nhật món ăn!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
        food.setIsActive(false);
        foodRepository.save(food);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa món ăn"));
    }

    private String joinGoals(List<String> goals) {
        if (goals == null || goals.isEmpty()) return "";
        return String.join(",", goals);
    }
}
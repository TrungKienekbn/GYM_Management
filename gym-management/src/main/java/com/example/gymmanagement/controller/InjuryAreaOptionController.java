package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.response.ApiResponse;
import com.example.gymmanagement.entity.InjuryAreaOption;
import com.example.gymmanagement.repository.InjuryAreaOptionRepository;
import com.example.gymmanagement.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/injury-areas")
@RequiredArgsConstructor
public class InjuryAreaOptionController {
    private final InjuryAreaOptionRepository repository;
    private final ExerciseRepository exerciseRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InjuryAreaOption>>> getAll() {
        syncCustomAreasFromExercises();
        return ResponseEntity.ok(ApiResponse.success(repository.findAllByOrderByLabelAsc()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InjuryAreaOption>> create(@RequestBody Map<String, String> body) {
        String label = normalizedLabel(body.get("label"));
        if (repository.existsByLabelIgnoreCase(label)) throw new RuntimeException("Tên chấn thương đã tồn tại");
        String baseCode = slug(label);
        String code = baseCode;
        int suffix = 2;
        while (repository.existsByCodeIgnoreCase(code)) {
            code = baseCode + "_" + suffix++;
        }
        InjuryAreaOption saved = repository.save(InjuryAreaOption.builder().code(code).label(label).build());
        return ResponseEntity.ok(ApiResponse.success(saved, "Đã thêm chấn thương"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InjuryAreaOption>> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        InjuryAreaOption item = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy chấn thương"));
        String label = normalizedLabel(body.get("label"));
        boolean duplicate = repository.findAll().stream().anyMatch(other -> !other.getId().equals(id) && other.getLabel().equalsIgnoreCase(label));
        if (duplicate) throw new RuntimeException("Tên chấn thương đã tồn tại");
        item.setLabel(label);
        return ResponseEntity.ok(ApiResponse.success(repository.save(item), "Đã cập nhật chấn thương"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        InjuryAreaOption item = repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy chấn thương"));
        exerciseRepository.findAll().forEach(exercise -> {
            exercise.setSecondaryMuscleGroups(removeCsvValue(exercise.getSecondaryMuscleGroups(), item.getLabel(), item.getCode()));
            exercise.setContraindicatedInjuries(removeCsvValue(exercise.getContraindicatedInjuries(), item.getLabel(), item.getCode()));
            exerciseRepository.save(exercise);
        });
        repository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa chấn thương"));
    }

    private String normalizedLabel(String value) {
        String label = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (label.isBlank()) throw new RuntimeException("Vui lòng nhập tên chấn thương");
        if (label.length() > 100) throw new RuntimeException("Tên chấn thương tối đa 100 ký tự");
        return label;
    }

    private String slug(String value) {
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D');
        String code = ascii.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
        return code.isBlank() ? "INJURY_" + System.currentTimeMillis() : code;
    }

    private String removeCsvValue(String csv, String... values) {
        if (csv == null || csv.isBlank()) return csv;
        Set<String> removing = java.util.Arrays.stream(values).map(v -> v.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());
        return java.util.Arrays.stream(csv.split(",")).map(String::trim).filter(v -> !v.isBlank())
                .filter(v -> !removing.contains(v.toLowerCase(Locale.ROOT))).collect(java.util.stream.Collectors.joining(","));
    }

    /** Đưa các vùng tùy chỉnh đã nhập ở bài tập vào danh mục dùng chung. */
    private void syncCustomAreasFromExercises() {
        Set<String> standard = Set.of("CHEST", "BACK", "SHOULDERS", "ARMS", "LEGS", "CORE", "CARDIO", "FULL_BODY",
                "KNEE", "LOWER_BACK", "SHOULDER", "WRIST", "ELBOW", "ANKLE", "NECK");
        exerciseRepository.findAll().forEach(exercise -> {
            String combined = String.join(",",
                    exercise.getSecondaryMuscleGroups() == null ? "" : exercise.getSecondaryMuscleGroups(),
                    exercise.getContraindicatedInjuries() == null ? "" : exercise.getContraindicatedInjuries());
            for (String raw : combined.split(",")) {
                String label = raw.trim();
                if (label.isBlank() || standard.contains(label.toUpperCase(Locale.ROOT)) || repository.existsByLabelIgnoreCase(label)) continue;
                String baseCode = slug(label);
                String code = baseCode;
                int suffix = 2;
                while (repository.existsByCodeIgnoreCase(code)) code = baseCode + "_" + suffix++;
                repository.save(InjuryAreaOption.builder().code(code).label(label).build());
            }
        });
    }
}

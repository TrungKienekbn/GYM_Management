package com.example.gymmanagement.service;

import com.example.gymmanagement.entity.SystemConfig;
import com.example.gymmanagement.repository.SystemConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repository;
    private final Map<String, Double> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadCache() {
        ensureConfig("LOW_COMPLETION_THRESHOLD", 40.0, "Điều chỉnh theo tuần",
                "Nếu tỷ lệ hoàn thành trung bình của tuần thấp hơn mức này, hệ thống áp dụng chiến lược đã chọn.");
        ensureConfig("LOW_COMPLETION_ACTION", 1.0, "Điều chỉnh theo tuần",
                "Chiến lược khi hoàn thành thấp 2 tuần liên tiếp: 1 = đổi sang bài dễ hơn, 2 = giảm sets/reps, 3 = thay ngẫu nhiên 1 bài tương đương.");
        ensureConfig("LOW_COMPLETION_SETS_REDUCTION", 1.0, "Điều chỉnh theo tuần",
                "Số sets giảm ở mỗi bài khi chọn chiến lược giảm khối lượng.");
        ensureConfig("LOW_COMPLETION_REPS_REDUCTION", 2.0, "Điều chỉnh theo tuần",
                "Số reps giảm ở mỗi bài khi chọn chiến lược giảm khối lượng.");
        repository.findById("LOW_COMPLETION_THRESHOLD").filter(c -> c.getConfigValue() != null && c.getConfigValue() == 60.0)
                .ifPresent(c -> { c.setConfigValue(40.0); repository.save(c); });
        if (repository.existsById("LOW_COMPLETION_CONSECUTIVE_WEEKS")) {
            repository.deleteById("LOW_COMPLETION_CONSECUTIVE_WEEKS");
        }
        if (repository.existsById("LOW_COMPLETION_RANDOM_ADD_COUNT")) {
            repository.deleteById("LOW_COMPLETION_RANDOM_ADD_COUNT");
        }
        repository.findById("LOW_COMPLETION_ACTION").filter(c -> c.getConfigValue() != null && c.getConfigValue() == 3.0)
                .ifPresent(c -> { c.setConfigValue(1.0); repository.save(c); });
        repository.findAll().stream().filter(c -> c.getConfigKey().startsWith("LOW_COMPLETION_"))
                .forEach(c -> {
                    c.setCategory("Điều chỉnh theo từng lần tập");
                    if ("LOW_COMPLETION_ACTION".equals(c.getConfigKey()))
                        c.setDescription("Cách xử lý khi cùng một bài có 2 lần thực hiện gần nhất dưới ngưỡng: 1 = đổi sang bài dễ hơn, 2 = giảm sets/reps trong ngưỡng mục tiêu.");
                    if ("LOW_COMPLETION_THRESHOLD".equals(c.getConfigKey()))
                        c.setDescription("Ngưỡng % áp dụng riêng cho từng bài. Hai lần thực hiện gần nhất của cùng bài đều dưới ngưỡng mới xử lý.");
                    repository.save(c);
                });
        repository.findAll().forEach(c -> cache.put(c.getConfigKey(), c.getConfigValue()));
    }

    private void ensureConfig(String key, double value, String category, String description) {
        if (!repository.existsById(key)) {
            repository.save(SystemConfig.builder().configKey(key).configValue(value)
                    .category(category).description(description).build());
        }
    }

    /** Đọc giá trị config; nếu chưa có trong DB thì trả về defaultValue (an toàn, không bao giờ NPE). */
    public double get(String key, double defaultValue) {
        Double v = cache.get(key);
        return v != null ? v : defaultValue;
    }

    public List<SystemConfig> getAll() {
        return repository.findAllByOrderByCategoryAscConfigKeyAsc();
    }

    public SystemConfig update(String key, Double newValue) {
        SystemConfig cfg = repository.findById(key)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy config: " + key));
        cfg.setConfigValue(newValue);
        repository.save(cfg);
        cache.put(key, newValue); // cập nhật cache ngay -> áp dụng công thức mới ngay lập tức
        return cfg;
    }
}

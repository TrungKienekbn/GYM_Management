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
        repository.findAll().forEach(c -> cache.put(c.getConfigKey(), c.getConfigValue()));
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
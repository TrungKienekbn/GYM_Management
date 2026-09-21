package com.example.gymmanagement.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductAttributeService {
    private final ProductAttributeRepository attributes;
    private final ProductAttributeValueRepository values;

    @Transactional
    public ProductAttribute createAttribute(String name) {
        if (name == null || name.isBlank()) throw new RuntimeException("Tên thuộc tính không được để trống");
        return attributes.save(ProductAttribute.builder().name(name.trim()).build());
    }

    public List<Map<String, Object>> allAttributes() {
        return attributes.findAllByOrderByNameAsc().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("name", a.getName());
            m.put("values", values.findByAttributeIdOrderByValueAsc(a.getId()).stream()
                    .map(v -> Map.of("id", v.getId(), "value", v.getValue())).toList());
            return m;
        }).toList();
    }

    @Transactional
    public void deleteAttribute(Long id) {
        values.deleteAll(values.findByAttributeIdOrderByValueAsc(id));
        attributes.deleteById(id);
    }

    @Transactional
    public ProductAttributeValue addValue(Long attributeId, String value) {
        if (value == null || value.isBlank()) throw new RuntimeException("Giá trị không được để trống");
        ProductAttribute a = attributes.findById(attributeId).orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính"));
        return values.save(ProductAttributeValue.builder().attribute(a).value(value.trim()).build());
    }

    @Transactional
    public void deleteValue(Long id) {
        values.deleteById(id);
    }
}
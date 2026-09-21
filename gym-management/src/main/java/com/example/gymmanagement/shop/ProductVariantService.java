package com.example.gymmanagement.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductVariantService {
    private final ShopAuditService audit; private final ProductVariantRepository variants;
    private final ProductRepository products;
    private final ProductAttributeValueRepository attributeValues;

    @Transactional
    public Map<String, Object> create(Long productId, Map<String, Object> req) {
        Product p = products.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        Integer stock = req.get("stock") == null ? 0 : Integer.valueOf(String.valueOf(req.get("stock")));
        if (stock < 0) throw new RuntimeException("Tồn kho không hợp lệ");
        Double priceOverride = req.get("priceOverride") == null ? null : Double.valueOf(String.valueOf(req.get("priceOverride")));
        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) req.getOrDefault("attributeValueIds", List.of());
        List<Long> ids = rawIds.stream().map(o -> Long.valueOf(String.valueOf(o))).toList();
        if (ids.isEmpty()) throw new RuntimeException("Vui lòng chọn ít nhất 1 giá trị thuộc tính cho biến thể");
        Set<ProductAttributeValue> valueSet = new HashSet<>(attributeValues.findByIdIn(ids));

        ProductVariant v = ProductVariant.builder().product(p).sku(str(req.get("sku")))
                .stock(stock).priceOverride(priceOverride).active(true).attributeValues(valueSet).build();
        variants.save(v);audit.stock(productId,v.getId(),0,v.getStock(),null,"Tạo phân loại");return map(v);
    }

    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> req) {
        ProductVariant v = variants.findLocked(id).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));
        int before=v.getStock();
        if (req.containsKey("stock")) v.setStock(Integer.valueOf(String.valueOf(req.get("stock"))));
        if (req.containsKey("priceOverride")) v.setPriceOverride(req.get("priceOverride") == null ? null : Double.valueOf(String.valueOf(req.get("priceOverride"))));
        if (req.containsKey("sku")) v.setSku(str(req.get("sku")));
        if (req.containsKey("active")) v.setActive(Boolean.valueOf(String.valueOf(req.get("active"))));
        if (v.getStock() < 0 || (v.getPriceOverride() != null && v.getPriceOverride() < 0))
            throw new IllegalArgumentException("Giá/tồn kho không hợp lệ");
        variants.save(v);
        audit.stock(v.getProduct().getId(),v.getId(),before,v.getStock(),null,"Điều chỉnh phân loại");
        return map(v);
    }

    @Transactional
    public void delete(Long id) {
        ProductVariant v=variants.findLocked(id).orElseThrow();v.setActive(false);variants.save(v);
    }

    public List<Map<String, Object>> forProduct(Long productId) {
        return variants.findByProductIdOrderByIdAsc(productId).stream().map(this::map).toList();
    }

    private String str(Object o) { return o == null ? "" : String.valueOf(o).trim(); }

    private Map<String, Object> map(ProductVariant v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("productId", v.getProduct().getId());
        m.put("sku", v.getSku());
        m.put("stock", v.getStock());
        m.put("priceOverride", v.getPriceOverride());
        m.put("active", v.getActive());
        m.put("attributeValues", v.getAttributeValues().stream()
                .sorted(Comparator.comparing(av -> av.getAttribute().getName()))
                .map(av -> Map.of("attributeId", av.getAttribute().getId(), "attributeName", av.getAttribute().getName(), "valueId", av.getId(), "value", av.getValue()))
                .toList());
        m.put("label", v.getAttributeValues().stream()
                .sorted(Comparator.comparing(av -> av.getAttribute().getName()))
                .map(av -> av.getAttribute().getName() + ": " + av.getValue())
                .reduce((a, b) -> a + ", " + b).orElse(""));
        return m;
    }
}

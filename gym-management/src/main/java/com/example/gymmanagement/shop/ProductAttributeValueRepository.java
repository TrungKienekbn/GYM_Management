package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    List<ProductAttributeValue> findByAttributeIdOrderByValueAsc(Long attributeId);
    List<ProductAttributeValue> findByIdIn(List<Long> ids);
}
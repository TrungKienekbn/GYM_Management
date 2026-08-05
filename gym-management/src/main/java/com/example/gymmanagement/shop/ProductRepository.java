package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
public interface ProductRepository extends JpaRepository<Product,Long>{ List<Product> findByActiveTrueOrderByCreatedAtDesc(); @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id") Optional<Product> findLocked(@Param("id")Long id); }

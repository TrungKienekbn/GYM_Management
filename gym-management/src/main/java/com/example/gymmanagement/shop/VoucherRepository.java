package com.example.gymmanagement.shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);
    List<Voucher> findAllByOrderByCreatedAtDesc();
}
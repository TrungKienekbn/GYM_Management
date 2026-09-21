package com.example.gymmanagement.shop;
public interface CustomerAddressRepository extends org.springframework.data.jpa.repository.JpaRepository<CustomerAddress,Long> { java.util.List<CustomerAddress> findByUserIdOrderByIdAsc(Long userId); }

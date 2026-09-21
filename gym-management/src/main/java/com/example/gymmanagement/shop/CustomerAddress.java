package com.example.gymmanagement.shop;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="customer_addresses")
@Getter @Setter @NoArgsConstructor
public class CustomerAddress { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long userId; private String receiverName; private String phone; @Column(length=1000) private String address; private boolean defaultAddress; }

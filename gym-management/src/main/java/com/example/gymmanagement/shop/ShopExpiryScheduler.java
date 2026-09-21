package com.example.gymmanagement.shop;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Component;import org.springframework.scheduling.annotation.Scheduled;
@Component @RequiredArgsConstructor public class ShopExpiryScheduler {private final ShopService shop;@Scheduled(fixedDelay=30000)public void expire(){shop.expire();}}

package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.response.*;
import com.example.gymmanagement.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MembershipResponse>>> getMyMemberships(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getMyMemberships(userDetails.getUsername())));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<MembershipResponse>> getActiveMembership(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(membershipService.getActiveMembership(userDetails.getUsername())));
    }
}

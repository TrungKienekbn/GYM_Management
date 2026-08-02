package com.example.gymmanagement.pet;

import com.example.gymmanagement.dto.response.ApiResponse;
import com.example.gymmanagement.pet.CosmeticItemResponse;
import com.example.gymmanagement.pet.PetResponse;
import com.example.gymmanagement.pet.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pet/cosmetics")
@RequiredArgsConstructor
public class PetCosmeticController {

    private final PetService petService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CosmeticItemResponse>>> catalog(
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(petService.getCatalog(ud.getUsername())));
    }

    @PostMapping("/{code}/equip")
    public ResponseEntity<ApiResponse<PetResponse>> equip(
            @AuthenticationPrincipal UserDetails ud, @PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(petService.equip(ud.getUsername(), code), "Đã trang bị"));
    }
}
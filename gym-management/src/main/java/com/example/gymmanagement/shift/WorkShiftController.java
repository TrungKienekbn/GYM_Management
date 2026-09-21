package com.example.gymmanagement.shift;

import com.example.gymmanagement.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class WorkShiftController {
    private final WorkShiftService shiftService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> assign(@RequestBody Map<String, Object> body) {
        Long staffUserId = Long.valueOf(String.valueOf(body.get("staffUserId")));
        LocalDate date = LocalDate.parse(String.valueOf(body.get("shiftDate")));
        LocalTime start = LocalTime.parse(String.valueOf(body.get("startTime")));
        LocalTime end = LocalTime.parse(String.valueOf(body.get("endTime")));
        return ResponseEntity.ok(ApiResponse.success(shiftService.assign(staffUserId, date, start, end), "Đã phân ca"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(ApiResponse.success(shiftService.allShifts()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STAFF')")
    public ResponseEntity<?> mine(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.success(shiftService.myShifts(u.getUsername())));
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STAFF')")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal UserDetails u, @PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Double cashAtStart = body != null && body.get("cashAtStart") != null ? Double.valueOf(String.valueOf(body.get("cashAtStart"))) : null;
        return ResponseEntity.ok(ApiResponse.success(shiftService.checkIn(u.getUsername(), id, cashAtStart), "Check-in thành công"));
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STAFF')")
    public ResponseEntity<?> checkOut(@AuthenticationPrincipal UserDetails u, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Double cashCounted = body.get("cashCounted") != null ? Double.valueOf(String.valueOf(body.get("cashCounted"))) : null;
        String note = body.get("handoverNote") == null ? null : String.valueOf(body.get("handoverNote"));
        return ResponseEntity.ok(ApiResponse.success(shiftService.checkOut(u.getUsername(), id, cashCounted, note), "Check-out thành công"));
    }
}
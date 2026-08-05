package com.example.gymmanagement.service;

import com.example.gymmanagement.dto.request.ResetPasswordByPhoneRequest;
import com.example.gymmanagement.entity.User;
import com.example.gymmanagement.repository.RoleRepository;
import com.example.gymmanagement.repository.UserRepository;
import com.example.gymmanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock RoleRepository roleRepository;
    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    private AuthService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new AuthService(roleRepository, userRepository, jwtService, passwordEncoder, emailService);
        user = User.builder().id(7L).email("test@gym.com").phone("0912345678")
                .password("old-hash").status(true).build();
        when(userRepository.findByEmail("test@gym.com")).thenReturn(Optional.of(user));
    }

    @Test
    void correctLastFourResetsPasswordWithoutIssuingLoginToken() {
        when(passwordEncoder.encode("newpass123")).thenReturn("new-hash");
        String result = service.resetPasswordWithPhoneLast4(request("5678", "newpass123"));

        assertEquals("Đặt lại mật khẩu thành công", result);
        assertEquals("new-hash", user.getPassword());
        assertEquals(0, user.getPasswordResetAttempts());
        verify(jwtService, never()).generateToken(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void fiveWrongAttemptsTemporarilyBlockFurtherAttempts() {
        for (int i = 0; i < 5; i++) {
            assertThrows(RuntimeException.class,
                    () -> service.resetPasswordWithPhoneLast4(request("0000", "newpass123")));
        }
        assertNotNull(user.getPasswordResetBlockedUntil());
        RuntimeException blocked = assertThrows(RuntimeException.class,
                () -> service.resetPasswordWithPhoneLast4(request("5678", "newpass123")));
        assertTrue(blocked.getMessage().contains("15 phút"));
        verify(passwordEncoder, never()).encode(anyString());
    }

    private ResetPasswordByPhoneRequest request(String lastFour, String newPassword) {
        ResetPasswordByPhoneRequest request = new ResetPasswordByPhoneRequest();
        request.setEmail("test@gym.com");
        request.setLastFourDigits(lastFour);
        request.setNewPassword(newPassword);
        return request;
    }
}

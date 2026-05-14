package com.docqa;

import com.docqa.model.User;
import com.docqa.repository.UserRepository;
import com.docqa.security.JwtUtil;
import com.docqa.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole("USER");
    }

    @Test
    void register_ShouldReturnToken_WhenEmailIsNew() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        Map<String, Object> result = authService.register("test@example.com", "secret");

        assertEquals("jwt-token", result.get("token"));
        assertEquals("test@example.com", result.get("email"));
    }

    @Test
    void register_ShouldThrow_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.register("test@example.com", "pass"));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("secret", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt-token");

        Map<String, Object> result = authService.login("test@example.com", "secret");

        assertEquals("jwt-token", result.get("token"));
    }

    @Test
    void login_ShouldThrow_WhenPasswordIsWrong() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.login("test@example.com", "wrongpass"));
    }

    @Test
    void login_ShouldThrow_WhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.login("unknown@example.com", "pass"));
    }
}

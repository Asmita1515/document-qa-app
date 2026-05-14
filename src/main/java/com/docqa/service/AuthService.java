package com.docqa.service;

import com.docqa.model.User;
import com.docqa.repository.UserRepository;
import com.docqa.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new user.
     */
    public Map<String, Object> register(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword)); // never store plain password
        user.setRole("USER");
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(email);
        return Map.of(
                "token", token,
                "userId", saved.getId(),
                "email", saved.getEmail()
        );
    }

    /**
     * Login an existing user.
     */
    public Map<String, Object> login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(email);
        return Map.of(
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail()
        );
    }
}

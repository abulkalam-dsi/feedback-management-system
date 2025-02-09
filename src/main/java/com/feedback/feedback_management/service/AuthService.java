package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.AuthRequestDTO;
import com.feedback.feedback_management.dto.AuthResponseDTO;
import com.feedback.feedback_management.dto.RegisterRequstDTO;
import com.feedback.feedback_management.dto.UserResponseDTO;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.UserRole;
import com.feedback.feedback_management.repository.UserRepository;
import com.feedback.feedback_management.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(RegisterRequstDTO requestDTO) {
        System.out.println("Received Registration Request for: " + requestDTO.getEmail());
        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already in use.");
        }

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(requestDTO.getRole().toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Valid roles are USER, APPROVER, ADMIN.");
        }

        User user = new User();
        user.setEmail(requestDTO.getEmail());
        user.setName(requestDTO.getName());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(userRole);

        userRepository.save(user);

        System.out.println("User Registered Successfully: " + user.getEmail());

        return "User registered successfully";
    }

    public AuthResponseDTO login(AuthRequestDTO requestDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestDTO.getEmail(), requestDTO.getPassword()));

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        return new AuthResponseDTO(token);
    }

    public void logout(String token) {
        jwtUtil.invalidateToken(token.replace("Bearer", ""));
    }
}

package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.AuthRequestDTO;
import com.feedback.feedback_management.dto.AuthResponseDTO;
import com.feedback.feedback_management.dto.RegisterRequstDTO;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.UserRole;
import com.feedback.feedback_management.exception.CustomException;
import com.feedback.feedback_management.repository.UserRepository;
import com.feedback.feedback_management.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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
        logger.info("Received Registration Request for email: {}", requestDTO.getEmail());
        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            logger.warn("Registration failed - Email already in use: {}", requestDTO.getEmail());
            throw new CustomException("Email is already in use.", HttpStatus.CONFLICT);
        }

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(requestDTO.getRole().toString().toUpperCase());
            logger.debug("User role assigned: {}", userRole);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role provided: {}", requestDTO.getRole());
            throw new CustomException("Invalid role. Valid roles are USER, APPROVER, ADMIN.", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setEmail(requestDTO.getEmail());
        user.setName(requestDTO.getName());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(userRole);
        logger.info("User registered successfully: {}", user.getEmail());

        userRepository.save(user);

        return "User registered successfully";
    }

    public AuthResponseDTO login(AuthRequestDTO requestDTO) {
        logger.info("Login attempt for email: {}", requestDTO.getEmail());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestDTO.getEmail(), requestDTO.getPassword()));
        } catch (AuthenticationException e) {
            logger.warn("Failed login attempt for email: {}", requestDTO.getEmail());
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - User not found: {}", requestDTO.getEmail());
                    return new CustomException("User not found", HttpStatus.NOT_FOUND);
                });

        String token = jwtUtil.generateToken(user);

        return new AuthResponseDTO(token);
    }

    public void logout(String token) {
        logger.info("Logout request received");
        try {
            jwtUtil.invalidateToken(token.replace("Bearer ", ""));
            logger.info("Token invalidated successfully");
        } catch (Exception e) {
            logger.error("Error invalidating token: {}", e.getMessage(), e);
            throw new CustomException("Error during logout", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.UserRequestDTO;
import com.feedback.feedback_management.dto.UserResponseDTO;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.enums.UserRole;
import com.feedback.feedback_management.exception.CustomException;
import com.feedback.feedback_management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        logger.info("Received user registration request for email: {}", userRequestDTO.getEmail());

        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(userRequestDTO.getEmail());

        if (existingUser.isPresent()) {
            logger.warn("Registration failed - Email already exists: {}", userRequestDTO.getEmail());
            throw new CustomException("Email already exists.", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(userRequestDTO.getPassword()); // Ensure password is hashed in real cases
        user.setRole(userRequestDTO.getRole());

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with email: {}", savedUser.getEmail());

        return new UserResponseDTO(savedUser);
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users from the database");
        List<User> users = userRepository.findAll();
        logger.info("Found {} user(s) in the database", users.size());
        return users;
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    logger.info("User found with ID: {}", id);
                    return new UserResponseDTO(user);
                });
    }

    public UserResponseDTO getCurrentUser(String email) {
        logger.info("Fetching current user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new CustomException("User not found", HttpStatus.NOT_FOUND);
                });

        logger.info("Current user retrieved: {}", user.getEmail());
        return new UserResponseDTO(user);
    }

    public void updateUserRole(Long id, String newRole) {
        logger.info("Updating role for user ID: {} to {}", id, newRole);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new CustomException("User not found", HttpStatus.NOT_FOUND);
                });

        user.setRole(UserRole.valueOf(newRole.toUpperCase()));
        userRepository.save(user);
        logger.info("User role updated successfully for ID: {}", id);
    }
}

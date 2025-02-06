package com.feedback.feedback_management.service;

import com.feedback.feedback_management.dto.UserRequestDTO;
import com.feedback.feedback_management.dto.UserResponseDTO;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO) {
        //Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(userRequestDTO.getEmail());

        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already in use.");
        }

        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(userRequestDTO.getPassword());
        user.setRole(userRequestDTO.getRole());

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(savedUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::new);
    }
}

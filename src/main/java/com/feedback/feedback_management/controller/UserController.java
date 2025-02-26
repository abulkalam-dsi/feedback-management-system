package com.feedback.feedback_management.controller;

import com.feedback.feedback_management.dto.FeedbackResponseDTO;
import com.feedback.feedback_management.dto.RoleUpdateRequestDTO;
import com.feedback.feedback_management.dto.UserRequestDTO;
import com.feedback.feedback_management.dto.UserResponseDTO;
import com.feedback.feedback_management.entity.User;
import com.feedback.feedback_management.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            UserResponseDTO userResponseDTO = userService.registerUser(userRequestDTO);
            return ResponseEntity.ok().body(userResponseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable long id) {
        return  userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDTO responseDTO = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable long id, @RequestBody RoleUpdateRequestDTO requestDTO) {
        userService.updateUserRole(id, requestDTO.getRole());
        return ResponseEntity.ok("User role updated successfully");
    }

}

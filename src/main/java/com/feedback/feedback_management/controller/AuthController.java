package com.feedback.feedback_management.controller;

import com.feedback.feedback_management.dto.AuthRequestDTO;
import com.feedback.feedback_management.dto.AuthResponseDTO;
import com.feedback.feedback_management.dto.RegisterRequstDTO;
import com.feedback.feedback_management.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequstDTO requstDTO) {
        return ResponseEntity.ok(authService.register(requstDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.login(requestDTO));
    }
}

package com.example.javabackend.controller;

import com.example.javabackend.model.User;
import com.example.javabackend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/verify")
    public ResponseEntity<User> verifyToken() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
} 
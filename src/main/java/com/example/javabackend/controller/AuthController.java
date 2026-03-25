package com.example.javabackend.controller;

import com.example.javabackend.dto.AuthTokens;
import com.example.javabackend.dto.LoginRequest;
import com.example.javabackend.model.User;
import com.example.javabackend.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/verify")
    public ResponseEntity<User> verifyToken() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) throws JsonProcessingException {
        // ObjectMapper mapper = new ObjectMapper();
        // AuthTokens tokens = authService.authenticate(mapper.readTree(request).get("email").toString(), mapper.readTree(request).get("password").toString());
        AuthTokens tokens = authService.authenticate(request.getEmail(), request.getPassword());

        // Set HttpOnly cookies
        Cookie accessToken = new Cookie("accessToken", tokens.getIdToken());
        accessToken.setHttpOnly(true);
        accessToken.setSecure(true);
        accessToken.setPath("/");
        response.addCookie(accessToken);
        Cookie idToken = new Cookie("idToken", tokens.getAccessToken());
        idToken.setHttpOnly(true);
        idToken.setSecure(true);
        idToken.setPath("/");
        response.addCookie(idToken);
        Cookie refreshToken = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshToken.setHttpOnly(true);
        refreshToken.setSecure(true);
        refreshToken.setPath("/");
        response.addCookie(refreshToken);
        return ResponseEntity.ok().body("Login successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("tokens") AuthTokens initTokens, HttpServletResponse response) throws JsonProcessingException {
        AuthTokens refreshedTokens = authService.refresh(initTokens.getRefreshToken());

        // Set HttpOnly cookies
        Cookie accessToken = new Cookie("accessToken", refreshedTokens.getIdToken());
        accessToken.setHttpOnly(true);
        accessToken.setSecure(true);
        accessToken.setPath("/");
        response.addCookie(accessToken);
        Cookie idToken = new Cookie("idToken", refreshedTokens.getAccessToken());
        idToken.setHttpOnly(true);
        idToken.setSecure(true);
        idToken.setPath("/");
        response.addCookie(idToken);
        Cookie refreshToken = new Cookie("refreshToken", refreshedTokens.getRefreshToken());
        refreshToken.setHttpOnly(true);
        refreshToken.setSecure(true);
        refreshToken.setPath("/");
        response.addCookie(refreshToken);

        return ResponseEntity.ok().body("Login successful");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("idToken", "");
        tokenCookie.setMaxAge(0);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
        tokenCookie = new Cookie("accessToken", "");
        tokenCookie.setMaxAge(0);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
        tokenCookie = new Cookie("refreshToken", "");
        tokenCookie.setMaxAge(0);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
        return ResponseEntity.ok().build();
    }

} 
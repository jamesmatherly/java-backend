package com.example.javabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthTokens {
    private String idToken;
    private String accessToken;
    private String refreshToken;
}

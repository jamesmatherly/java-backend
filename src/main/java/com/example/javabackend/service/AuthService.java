package com.example.javabackend.service;

import com.example.javabackend.dto.AuthTokens;
import com.example.javabackend.model.User;
import com.example.javabackend.repository.UserRepository;
import com.example.javabackend.security.CognitoUserDetailsService;
import com.example.javabackend.security.JwtTokenValidator;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final CognitoIdentityProviderClient cognitoClient;

    private final CognitoUserDetailsService cognitoUserDetailsService;

    private final JwtTokenValidator jwtTokenValidator;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    public AuthService(UserRepository userRepository, CognitoUserDetailsService cognitoUserDetailsService, JwtTokenValidator jwtTokenValidator) {
        this.userRepository = userRepository;
        this.cognitoClient = CognitoIdentityProviderClient.create();
        this.cognitoUserDetailsService = cognitoUserDetailsService;
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Transactional
    public User getCurrentUser() {
        String cognitoId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(cognitoId)
                .orElseThrow(() -> new RuntimeException("User not found in database"));
    }

    public AuthTokens authenticate(String email, String password) {
        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(Map.of("USERNAME", email, "PASSWORD", password))
                .build();

        AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

        String idToken = authResponse.authenticationResult().idToken();
        String accessToken = authResponse.authenticationResult().accessToken();
        String cognitoId = (String) jwtTokenValidator.toAuthentication(accessToken).getPrincipal();
        cognitoUserDetailsService.ensureUser(cognitoId, email);

        return new AuthTokens(
                idToken,
                accessToken,
                authResponse.authenticationResult().refreshToken()
        );
    }

    public AuthTokens refresh(String refreshToken) {
        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(Map.of("REFRESH_TOKEN", refreshToken))
                .build();

        AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

        return new AuthTokens(
                authResponse.authenticationResult().idToken(),
                authResponse.authenticationResult().accessToken(),
                authResponse.authenticationResult().refreshToken()
        );
    }


} 
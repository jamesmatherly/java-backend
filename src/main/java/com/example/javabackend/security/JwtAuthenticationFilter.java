package com.example.javabackend.security;

import com.example.javabackend.dto.AuthTokens;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final String clientId;
    private final String userPoolId;
    private final JwtTokenValidator jwtValidator;
    private final ObjectMapper objectMapper;
    private final CognitoIdentityProviderClient cognitoClient;

    public JwtAuthenticationFilter(
            @Value("${aws.cognito.clientId}") String clientId,
            @Value("${aws.cognito.userPoolId}") String userPoolId,
            JwtTokenValidator tokenValidator) {
        this.clientId = clientId;
        this.userPoolId = userPoolId;
        this.jwtValidator = tokenValidator;
        CognitoIdentityProviderClientBuilder builder = CognitoIdentityProviderClient.builder();
        this.cognitoClient = builder.build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().contains("auth/")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getCookies() == null) {
            logger.debug("Received request to: {} with no cookies found");
            handleError(response, "No valid Authorization header found", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Cookie idCookie = null, accessCookie = null, refreshCookie = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("idToken")) {
                idCookie = cookie;
            }
            if (cookie.getName().equals("accessToken")) {
                accessCookie = cookie;
            }
            if (cookie.getName().equals("refreshToken")) {
                refreshCookie = cookie;
            }
        }
        if (idCookie == null || accessCookie == null || refreshCookie == null) {
            logger.debug("Received request to: {} with missing cookies", request.getRequestURI());
            handleError(response, "Received request to: {} with missing cookies", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // logger.debug("Received request to: {} with tokens cookie", request.getRequestURI(), authHeader);
        
        String accessToken = accessCookie.getValue();
        String idToken = idCookie.getValue();
        String refreshToken = refreshCookie.getValue();
        try {
            // Validate access token (signature, exp, iss, aud)
            jwtValidator.validate(accessToken);

            // If valid, set authentication context
            Authentication authentication = jwtValidator.toAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            // Token expired -> try refresh
            if (refreshToken != null) {
                try {
                    AuthenticationResultType newTokens = refreshTokens(refreshToken);

                    // Set new cookies
                    AuthTokens tokens = new AuthTokens(newTokens.idToken(), newTokens.accessToken(), newTokens.refreshToken());
                    JsonMapper mappper = new JsonMapper();
                    Cookie tokenCookie = new Cookie("tokens", mappper.writeValueAsString(tokens));
                    tokenCookie.setHttpOnly(true);
                    tokenCookie.setSecure(true);
                    tokenCookie.setPath("/");
                    response.addCookie(tokenCookie);


                    // Set authentication
                    jwtValidator.validate(newTokens.accessToken());
                    Authentication authentication = jwtValidator.toAuthentication(newTokens.accessToken());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    filterChain.doFilter(request, response);
                } catch (Exception refreshEx) {
                    clearCookies(response);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
                }
            } else {
                clearCookies(response);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing refresh token");
            }
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }


    }

    private void handleError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        
        objectMapper.writeValue(response.getWriter(), error);
    }

    private AuthenticationResultType refreshTokens(String refreshToken) {
        AdminInitiateAuthRequest refreshRequest = AdminInitiateAuthRequest.builder()
            .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
            .userPoolId(userPoolId)
            .clientId(clientId)
            .authParameters(Map.of("REFRESH_TOKEN", refreshToken))
            .build();

        return cognitoClient.adminInitiateAuth(refreshRequest).authenticationResult();
    }

    private void clearCookies(HttpServletResponse response) {
        for (String name : List.of("accessToken", "idToken", "refreshToken")) {
            Cookie cookie = new Cookie(name, "");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }
} 
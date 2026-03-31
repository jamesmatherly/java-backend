package com.example.javabackend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class JwtTokenValidator {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);
    private final JwtDecoder jwtDecoder;

    public JwtTokenValidator(
            @Value("${aws.cognito.jwksUrl}") String jwksUrl
            ) {
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
    }

    public void validate(String token) {
        jwtDecoder.decode(token); // throws exception if invalid/expired
    }

    public Authentication toAuthentication(String accessToken) {
        Jwt jwt = jwtDecoder.decode(accessToken);
        Collection<GrantedAuthority> authorities = List.of(); // or map Cognito groups → roles
        return new UsernamePasswordAuthenticationToken(jwt.getClaimAsString("sub"), jwt, authorities);
    }

    public String extractEmail(String idToken) {
        Jwt jwt = jwtDecoder.decode(idToken);
        return jwt.getClaimAsString("email");
    }
} 
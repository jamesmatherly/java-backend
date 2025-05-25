package com.example.javabackend.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.text.ParseException;

@Component
public class JwtTokenValidator {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final String jwksUrl;

    public JwtTokenValidator(
            @Value("${aws.cognito.jwksUrl}") String jwksUrl,
            RestTemplate restTemplate) {
        this.jwksUrl = jwksUrl;
        this.jwtProcessor = new DefaultJWTProcessor<>();
        logger.info("Initializing JwtTokenValidator with JWKS URL: {}", jwksUrl);
        initializeJwtProcessor(restTemplate);
    }

    private void initializeJwtProcessor(RestTemplate restTemplate) {
        try {
            logger.debug("Fetching JWKS from URL: {}", jwksUrl);
            String jwksJson = restTemplate.getForObject(jwksUrl, String.class);
            if (jwksJson == null) {
                throw new RuntimeException("Failed to fetch JWKS from: " + jwksUrl);
            }

            logger.debug("Parsing JWKS JSON");
            JWKSet jwkSet = JWKSet.parse(jwksJson);
            JWKSource<SecurityContext> keySource = new ImmutableJWKSet<>(jwkSet);
            
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);
            jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
                // Verify the token is not expired
                if (claims.getExpirationTime() != null && claims.getExpirationTime().getTime() < System.currentTimeMillis()) {
                    throw new RuntimeException("Token has expired");
                }
            });
            logger.info("JWT processor initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize JWT processor", e);
            throw new RuntimeException("Failed to initialize JWT processor", e);
        }
    }

    public JWTClaimsSet validateToken(String token) throws ParseException {
        try {
            logger.debug("Validating token");
            JWTClaimsSet claims = jwtProcessor.process(token, null);
            logger.debug("Token validated successfully");
            return claims;
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String getUsernameFromToken(String token) throws ParseException {
        JWTClaimsSet claims = validateToken(token);
        String username = claims.getSubject();
        logger.debug("Extracted username from token: {}", username);
        return username;
    }

    public List<String> getGroupsFromToken(String token) throws ParseException {
        JWTClaimsSet claims = validateToken(token);
        List<String> groups = claims.getStringListClaim("cognito:groups");
        logger.debug("Extracted groups from token: {}", groups);
        return groups;
    }

    public String getEmailFromToken(String token) throws ParseException {
        JWTClaimsSet claims = validateToken(token);
        String username = claims.getStringClaim("username");
        logger.debug("Extracted email from token: {}", username);
        return username;
    }
} 
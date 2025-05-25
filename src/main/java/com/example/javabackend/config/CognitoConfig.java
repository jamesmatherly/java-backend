package com.example.javabackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
public class CognitoConfig {
    
    @Value("${aws.cognito.region}")
    private String region;
    
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;
    
    @Value("${aws.cognito.clientId}")
    private String clientId;
    
    @Value("${aws.cognito.jwksUrl}")
    private String jwksUrl;
    
    public Region getRegion() {
        return Region.of(region);
    }
    
    public String getUserPoolId() {
        return userPoolId;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public String getJwksUrl() {
        return jwksUrl;
    }
} 
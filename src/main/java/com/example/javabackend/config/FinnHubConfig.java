package com.example.javabackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

@Configuration
public class FinnHubConfig {
    
    @Value("${finnhub.api.key}")
    private String apiKey;
    
    @Value("${finnhub.api.base-url:https://finnhub.io/api/v1}")
    private String baseUrl;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
} 
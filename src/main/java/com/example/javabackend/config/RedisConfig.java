package com.example.javabackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.javabackend.dto.FinnHubLookupResponse;
import com.example.javabackend.dto.FinnHubQuoteResponse;

@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, FinnHubQuoteResponse> redisQuoteTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, FinnHubQuoteResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        Jackson2JsonRedisSerializer<FinnHubQuoteResponse> jsonSerializer = 
            new Jackson2JsonRedisSerializer<>(FinnHubQuoteResponse.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public RedisTemplate<String, FinnHubLookupResponse> redisLookupTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, FinnHubLookupResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        Jackson2JsonRedisSerializer<FinnHubLookupResponse> jsonSerializer = 
            new Jackson2JsonRedisSerializer<>(FinnHubLookupResponse.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
} 
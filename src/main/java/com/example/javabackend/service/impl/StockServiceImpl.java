package com.example.javabackend.service.impl;

import com.example.javabackend.config.FinnHubConfig;
import com.example.javabackend.dto.FinnHubQuoteResponse;
import com.example.javabackend.dto.StockDTO;
import com.example.javabackend.mapper.StockMapper;
import com.example.javabackend.service.StockService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@Transactional
public class StockServiceImpl implements StockService {
    private static final String CACHE_KEY_PREFIX = "stock:quote:";
    private static final Duration CACHE_DURATION = Duration.ofHours(72);
    
    private final RestTemplate restTemplate;
    private final FinnHubConfig finnHubConfig;
    private final RedisTemplate<String, FinnHubQuoteResponse> redisTemplate;
    private final StockMapper stockMapper;

    public StockServiceImpl(
            RestTemplate restTemplate,
            FinnHubConfig finnHubConfig,
            RedisTemplate<String, FinnHubQuoteResponse> redisTemplate,
            StockMapper stockMapper) {
        this.restTemplate = restTemplate;
        this.finnHubConfig = finnHubConfig;
        this.redisTemplate = redisTemplate;
        this.stockMapper = stockMapper;
    }

    @Override
    public FinnHubQuoteResponse getQuote(String symbol) {
        String cacheKey = CACHE_KEY_PREFIX + symbol;
        
        // Try to get from cache first
        FinnHubQuoteResponse cachedQuote = redisTemplate.opsForValue().get(cacheKey);
        if (cachedQuote != null) {
            return cachedQuote;
        }
        
        // If not in cache, fetch from API
        String url = String.format("%s/quote?symbol=%s", finnHubConfig.getBaseUrl(), symbol);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Finnhub-Token", finnHubConfig.getApiKey());
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<FinnHubQuoteResponse> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            FinnHubQuoteResponse.class
        );
        
        FinnHubQuoteResponse quote = response.getBody();
        
        // Cache the response
        if (quote != null) {
            redisTemplate.opsForValue().set(cacheKey, quote, CACHE_DURATION);
        }
        
        return quote;
    }

    @Override
    public StockDTO getStockInfo(String symbol) {
        FinnHubQuoteResponse quote = getQuote(symbol);
        if (quote == null) {
            throw new RuntimeException("Stock data not found");
        }
        return stockMapper.toStockDTO(quote, symbol);
    }
} 
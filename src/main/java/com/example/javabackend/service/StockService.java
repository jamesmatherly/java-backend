package com.example.javabackend.service;

import com.example.javabackend.dto.StockDTO;
import com.example.javabackend.dto.FinnHubQuoteResponse;

public interface StockService {
    StockDTO getStockInfo(String symbol);
    FinnHubQuoteResponse getQuote(String symbol);
} 
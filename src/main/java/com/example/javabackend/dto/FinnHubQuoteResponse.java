package com.example.javabackend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinnHubQuoteResponse {
    @JsonProperty("c")
    private BigDecimal currentPrice;
    
    @JsonProperty("d")
    private BigDecimal change;
    
    @JsonProperty("dp")
    private BigDecimal changePercent;
    
    @JsonProperty("h")
    private BigDecimal high;
    
    @JsonProperty("l")
    private BigDecimal low;
    
    @JsonProperty("o")
    private BigDecimal open;
    
    @JsonProperty("pc")
    private BigDecimal previousClose;
} 
package com.example.javabackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinnHubQuoteResponse {
    @JsonProperty("c")
    private Double currentPrice;
    
    @JsonProperty("d")
    private Double change;
    
    @JsonProperty("dp")
    private Double changePercent;
    
    @JsonProperty("h")
    private Double high;
    
    @JsonProperty("l")
    private Double low;
    
    @JsonProperty("o")
    private Double open;
    
    @JsonProperty("pc")
    private Double previousClose;
    
    @JsonProperty("v")
    private Long volume;
} 
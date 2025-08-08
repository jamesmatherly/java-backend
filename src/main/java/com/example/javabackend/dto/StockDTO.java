package com.example.javabackend.dto;

import lombok.Data;

@Data
public class StockDTO {
    private String symbol;
    private String name;
    private Double price;
    private Double change;
    private Double changePercent;
    private Double marketCap;
    private Double high;
    private Double low;
    private Double open;
    private Double previousClose;
} 
package com.example.javabackend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StockDTO {
    private String symbol;
    private String name;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal marketCap;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private BigDecimal previousClose;
} 
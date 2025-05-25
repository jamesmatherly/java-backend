package com.example.javabackend.dto;

import lombok.Data;

@Data
public class StockDTO {
    private String symbol;
    private String name;
    private double price;
    private double change;
    private double changePercent;
    private double marketCap;
    private long volume;
    private double high;
    private double low;
    private double open;
    private double previousClose;
} 
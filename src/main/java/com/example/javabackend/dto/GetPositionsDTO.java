package com.example.javabackend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class GetPositionsDTO {
    String id;
    String symbol;
    String name;
    Integer quantity;
    BigDecimal entryPrice;
    BigDecimal currentPrice;
    BigDecimal change;
    BigDecimal changePercent;
}

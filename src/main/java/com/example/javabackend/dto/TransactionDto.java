package com.example.javabackend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionDto {
    public enum TransactionType {
        BUY_TO_OPEN,
        BUY_TO_CLOSE,
        SELL_TO_OPEN,
        SELL_TO_CLOSE
    }

    private TransactionType transactionType;
    private String positionId;
    private String portfolioId;
    private String symbol;
    private BigDecimal limitPrice;
    private Integer quantity;
}

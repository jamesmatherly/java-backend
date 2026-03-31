package com.example.javabackend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreatePortfolioDTO {
    private BigDecimal value;
    private String name;
    private String description;
}

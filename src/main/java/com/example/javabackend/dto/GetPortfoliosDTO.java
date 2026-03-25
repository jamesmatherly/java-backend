package com.example.javabackend.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class GetPortfoliosDTO {
    String id;
    String name;
    BigDecimal balance;
    BigDecimal value;
    BigDecimal profit;
    BigDecimal profitPercent;
    List<GetPositionsDTO> positions;
}

package com.example.javabackend.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.javabackend.dto.GetPositionsDTO;
import com.example.javabackend.model.Position;

@Component
public class PositionMapper {
    GetPositionsDTO toGetPositionsDTO(Position position) {
        GetPositionsDTO retVal = new GetPositionsDTO();
        retVal.setId(position.getId());
        retVal.setSymbol(position.getStockSymbol());
        retVal.setName("");
        retVal.setQuantity(position.getQuantity());
        retVal.setEntryPrice(position.getAveragePrice());
        retVal.setCurrentPrice(position.getCurrentPrice());
        retVal.setChange(new BigDecimal(0));
        retVal.setChangePercent(new BigDecimal(0));
        return retVal;
    }
}

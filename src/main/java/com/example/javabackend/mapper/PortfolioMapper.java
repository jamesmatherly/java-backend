package com.example.javabackend.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.javabackend.dto.CreatePortfolioDTO;
import com.example.javabackend.dto.GetPortfoliosDTO;
import com.example.javabackend.dto.GetPositionsDTO;
import com.example.javabackend.model.Portfolio;
import com.example.javabackend.model.Position;

@Component
public class PortfolioMapper {
    public GetPortfoliosDTO toPortfoliosDTO(Portfolio portfolio) {
        GetPortfoliosDTO retVal = new GetPortfoliosDTO();
        PositionMapper mapper = new PositionMapper();
        BigDecimal value = new BigDecimal(0);
        BigDecimal costBasis = new BigDecimal(1);
        retVal.setBalance(new BigDecimal(0));
        List<GetPositionsDTO> positions = new ArrayList<>();
        for (Position p : portfolio.getPositions()) {
            if (p.getStockSymbol() == null) {
                retVal.setBalance(p.getCurrentPrice());
            }
            value.add(p.getCurrentPrice().multiply(new BigDecimal(p.getQuantity())));
            // value.add(p.getCostBasis().multiply(new BigDecimal(p.getQuantity())));
            positions.add(mapper.toGetPositionsDTO(p));
        }
        retVal.setId(portfolio.getId());
        retVal.setName(portfolio.getName());
        retVal.setPositions(positions);
        retVal.setProfit(value.subtract(costBasis));
        retVal.setProfitPercent(retVal.getProfit().divide(costBasis));
        retVal.setValue(value);
        return retVal;
    }

    public Portfolio createDtoToPortfolio(CreatePortfolioDTO dto) {
        Portfolio retVal = new Portfolio();
        retVal.setName(dto.getName());
        retVal.setDescription("description");
        Position cashPosition = new Position();
        cashPosition.setPortfolio(retVal);
        cashPosition.setCurrentPrice(dto.getValue());
        cashPosition.setQuantity(1);
        cashPosition.setAveragePrice(dto.getValue());
        // cashPosition.setCostBasis(dto.getValue());
        retVal.setPositions(List.of(cashPosition));
        return retVal;
    }
}

package com.example.javabackend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.javabackend.dto.StockDTO;
import com.example.javabackend.dto.TransactionDto;
import com.example.javabackend.model.Portfolio;
import com.example.javabackend.model.Position;
import com.example.javabackend.repository.PositionRepository;
import com.example.javabackend.service.PortfolioService;
import com.example.javabackend.service.PositionService;
import com.example.javabackend.service.StockService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PositionServiceImpl extends BaseServiceImpl<Position, String, PositionRepository> implements PositionService {
    private static final Logger logger = LoggerFactory.getLogger(PositionServiceImpl.class);

    PortfolioService portfolioService;

    StockService stockService;

    public PositionServiceImpl(PositionRepository repository, PortfolioService portfolioService, StockService stockService) {
        super(repository);
        this.portfolioService = portfolioService;
        this.stockService = stockService;
    }

    @Override
    public void sellToClose(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) {
            return;
        }
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        if (pArr == null || pArr[0] == null || pArr[0].getQuantity() < dto.getQuantity()) {
            return;
        }
        StockDTO stock = stockService.getStockInfo(dto.getSymbol());
        // BigDecimal prevCostBasis = pArr[0].getCostBasis();
        BigDecimal prevQuantity = new BigDecimal(pArr[0].getQuantity());
        // BigDecimal prevTotalCost = prevCostBasis.multiply(prevQuantity);
        // BigDecimal totalCost = prevTotalCost.subtract(prevCostBasis.multiply(new BigDecimal(dto.getQuantity())));
        BigDecimal quantity = new BigDecimal(dto.getQuantity());
        // BigDecimal costBasis = totalCost.divide(prevQuantity.subtract(quantity));
        // pArr[0].setCostBasis(costBasis);
        pArr[0].setQuantity(prevQuantity.subtract(quantity).intValue());
        pArr[0].setCurrentPrice(stock.getPrice());
        // pArr[0].setTotalValue(totalCost);
        pArr[1].setTotalValue(pArr[1].getTotalValue().add(stock.getPrice().multiply(new BigDecimal(dto.getQuantity()))));
        repository.save(pArr[0]);
        repository.save(pArr[1]);
    }

    @Override
    public void sellToOpen(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) {
            return;
        }
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        if (pArr == null) {
            return;
        }

        if (pArr == null || pArr[0].getQuantity() < dto.getQuantity()) {
            return;
        }

    }

    @Override
    public void buyToOpen(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) {
            return;
        }
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        StockDTO stock = stockService.getStockInfo(dto.getSymbol());
        if (pArr == null || pArr[1].getTotalValue().compareTo(stock.getPrice().multiply(new BigDecimal(dto.getQuantity()))) == -1) {
            return;
        }
        if (pArr[0] == null) {
            pArr[0] = new Position();
            pArr[0].setPortfolio(portfolio);
            // pArr[0].setCostBasis(stock.getPrice());
            pArr[0].setStockSymbol(dto.getSymbol());
        }
        

    }

    @Override
    public void buyToClose(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) {
            return;
        }
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        if (pArr == null) {
            return;
        }

    }

    @Override
    public List<Position> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<Position> findByPortfolioId(String portfolioId) {
        return repository.findByPortfolioId(portfolioId);
    }

    private Portfolio getParentPortfolio(TransactionDto dto) {
        Optional<Portfolio> op = portfolioService.findById(dto.getPortfolioId());
        if (op.isPresent()) {
            logger.error(String.format("Portfolio %s not found", dto.getPortfolioId()));
            return null;
        }
        return op.get();
    }

    private Position[] getTargetAndCash(Portfolio portfolio, TransactionDto dto) {
        Position target = null;
        Position cash = null;
        for (Position p : portfolio.getPositions()) {
            if (p.getStockSymbol().equals(dto.getSymbol())) {
                if (p.getQuantity() > dto.getQuantity()) {
                    target = p;
                }
            }
            if (p.getStockSymbol() == null) {
                cash = p;
            }
        }
        if (target == null) {
            return null;
        }
        if (cash == null) {
            cash = new Position();
        }
        return new Position[] {target, cash};
    }

    private void addToQueue(TransactionDto dto) {

    }
}

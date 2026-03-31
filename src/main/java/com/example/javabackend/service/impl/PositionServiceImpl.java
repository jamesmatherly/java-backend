package com.example.javabackend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

//TODO: Implement queues for Limit transactions
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

    // Closes an existing long position. Proceeds are credited to cash.
    @Override
    public void sellToClose(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) return;
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        if (pArr == null || pArr[0] == null || pArr[0].getQuantity() < dto.getQuantity()) return;

        StockDTO stock = stockService.getStockInfo(dto.getSymbol());
        BigDecimal salePrice = stock.getPrice();
        BigDecimal soldQuantity = new BigDecimal(dto.getQuantity());
        int remainingQuantity = pArr[0].getQuantity() - dto.getQuantity();

        // averagePrice (cost basis) is unchanged — it reflects what was paid, not the sale price
        pArr[0].setQuantity(remainingQuantity);
        pArr[0].setCurrentPrice(salePrice);
        pArr[0].setTotalValue(salePrice.multiply(new BigDecimal(remainingQuantity)));

        pArr[1].setTotalValue(pArr[1].getTotalValue().add(salePrice.multiply(soldQuantity)));

        if (remainingQuantity == 0) {
            repository.delete(pArr[0]);
        } else {
            repository.save(pArr[0]);
        }
        repository.save(pArr[1]);
    }

    // Opens a new short position. Proceeds from the short sale are credited to cash.
    @Override
    public void sellToOpen(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) return;
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        if (pArr == null) return;

        StockDTO stock = stockService.getStockInfo(dto.getSymbol());
        BigDecimal salePrice = stock.getPrice();
        BigDecimal saleQuantity = new BigDecimal(dto.getQuantity());

        if (pArr[0] == null) {
            // New short position — represented as negative quantity
            pArr[0] = new Position();
            pArr[0].setPortfolio(portfolio);
            pArr[0].setUser(portfolio.getUser());
            pArr[0].setStockSymbol(dto.getSymbol());
            pArr[0].setQuantity(-dto.getQuantity());
            pArr[0].setAveragePrice(salePrice);
        } else {
            // Adding to an existing short — update weighted average short price
            BigDecimal prevShortQuantity = new BigDecimal(Math.abs(pArr[0].getQuantity()));
            BigDecimal newTotalShortQuantity = prevShortQuantity.add(saleQuantity);
            BigDecimal newAveragePrice = pArr[0].getAveragePrice().multiply(prevShortQuantity)
                    .add(salePrice.multiply(saleQuantity))
                    .divide(newTotalShortQuantity, 10, RoundingMode.HALF_UP);
            pArr[0].setQuantity(-newTotalShortQuantity.intValue());
            pArr[0].setAveragePrice(newAveragePrice);
        }

        pArr[0].setCurrentPrice(salePrice);
        pArr[0].setTotalValue(salePrice.multiply(new BigDecimal(Math.abs(pArr[0].getQuantity()))));

        pArr[1].setTotalValue(pArr[1].getTotalValue().add(salePrice.multiply(saleQuantity)));

        repository.save(pArr[0]);
        repository.save(pArr[1]);
    }

    // Opens a new long position or adds to an existing one. Cost is deducted from cash.
    @Override
    public void buyToOpen(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) return;
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        if (pArr == null) return;

        StockDTO stock = stockService.getStockInfo(dto.getSymbol());
        BigDecimal purchasePrice = stock.getPrice();
        BigDecimal purchaseQuantity = new BigDecimal(dto.getQuantity());
        BigDecimal totalCost = purchasePrice.multiply(purchaseQuantity);

        if (pArr[1].getTotalValue().compareTo(totalCost) < 0) return;

        if (pArr[0] == null) {
            // New long position
            pArr[0] = new Position();
            pArr[0].setPortfolio(portfolio);
            pArr[0].setUser(portfolio.getUser());
            pArr[0].setStockSymbol(dto.getSymbol());
            pArr[0].setQuantity(dto.getQuantity());
            pArr[0].setAveragePrice(purchasePrice);
        } else {
            // Adding to existing position — recalculate weighted average price
            BigDecimal prevQuantity = new BigDecimal(pArr[0].getQuantity());
            BigDecimal newTotalQuantity = prevQuantity.add(purchaseQuantity);
            BigDecimal newAveragePrice = pArr[0].getAveragePrice().multiply(prevQuantity)
                    .add(totalCost)
                    .divide(newTotalQuantity, 10, RoundingMode.HALF_UP);
            pArr[0].setQuantity(newTotalQuantity.intValue());
            pArr[0].setAveragePrice(newAveragePrice);
        }

        pArr[0].setCurrentPrice(purchasePrice);
        pArr[0].setTotalValue(purchasePrice.multiply(new BigDecimal(pArr[0].getQuantity())));

        pArr[1].setTotalValue(pArr[1].getTotalValue().subtract(totalCost));

        repository.save(pArr[0]);
        repository.save(pArr[1]);
    }

    // Closes an existing short position by buying back shares. Cost is deducted from cash.
    @Override
    public void buyToClose(TransactionDto dto) {
        Portfolio portfolio = getParentPortfolio(dto);
        if (portfolio == null) return;
        if (dto.getLimitPrice() != null) {
            addToQueue(dto);
            return;
        }
        Position[] pArr = getTargetAndCash(portfolio, dto);
        if (pArr == null || pArr[0] == null || Math.abs(pArr[0].getQuantity()) < dto.getQuantity()) return;

        StockDTO stock = stockService.getStockInfo(dto.getSymbol());
        BigDecimal buyPrice = stock.getPrice();
        BigDecimal buyQuantity = new BigDecimal(dto.getQuantity());
        BigDecimal totalCost = buyPrice.multiply(buyQuantity);

        if (pArr[1].getTotalValue().compareTo(totalCost) < 0) return;

        int remainingShortQuantity = Math.abs(pArr[0].getQuantity()) - dto.getQuantity();
        pArr[0].setQuantity(-remainingShortQuantity);
        pArr[0].setCurrentPrice(buyPrice);
        pArr[0].setTotalValue(buyPrice.multiply(new BigDecimal(remainingShortQuantity)));

        pArr[1].setTotalValue(pArr[1].getTotalValue().subtract(totalCost));

        if (remainingShortQuantity == 0) {
            repository.delete(pArr[0]);
        } else {
            repository.save(pArr[0]);
        }
        repository.save(pArr[1]);
    }

    @Override
    public List<Position> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<Position> findByPortfolioId(String portfolioId) {
        return repository.findByPortfolioId(portfolioId);
    }

    @Override
    public List<Position> findByPortfolioIdAndUserId(String portfolioId, String userId) {
        return repository.findByPortfolioIdAndUserId(portfolioId, userId);
    }

    private Portfolio getParentPortfolio(TransactionDto dto) {
        Optional<Portfolio> op = portfolioService.findById(dto.getPortfolioId());
        if (!op.isPresent()) {
            logger.error("Portfolio {} not found", dto.getPortfolioId());
            return null;
        }
        return op.get();
    }

    // Returns [targetPosition, cashPosition]. targetPosition may be null if no existing
    // position for the symbol exists — callers that create new positions handle that case.
    // Returns null if the portfolio has no cash position.
    private Position[] getTargetAndCash(Portfolio portfolio, TransactionDto dto) {
        Position target = null;
        Position cash = null;
        for (Position p : portfolio.getPositions()) {
            if (p.getStockSymbol() != null && p.getStockSymbol().equals(dto.getSymbol())) {
                target = p;
            }
            if (p.getStockSymbol() == null) {
                cash = p;
            }
        }
        if (cash == null) {
            logger.error("No cash position found for portfolio {}", portfolio.getId());
            return null;
        }
        return new Position[] {target, cash};
    }

    private void addToQueue(TransactionDto dto) {

    }
}

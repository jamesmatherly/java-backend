package com.example.javabackend.service;

import java.util.List;

import com.example.javabackend.dto.TransactionDto;
import com.example.javabackend.model.Position;

public interface PositionService extends BaseService<Position, String> {
    public void sellToClose(TransactionDto dto);
    public void sellToOpen(TransactionDto dto);
    public void buyToOpen(TransactionDto dto);
    public void buyToClose(TransactionDto dto);
    public List<Position> findByUserId(String userId);
    public List<Position> findByPortfolioId(String portfolioId);
    public List<Position> findByPortfolioIdAndUserId(String portfolioId, String userId);
}

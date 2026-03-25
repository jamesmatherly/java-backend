package com.example.javabackend.service;

import com.example.javabackend.dto.CreatePortfolioDTO;
import com.example.javabackend.model.Portfolio;
import com.example.javabackend.model.User;

import java.util.List;

public interface PortfolioService extends BaseService<Portfolio, String> {
    List<Portfolio> findByUserId(String userId);
    boolean createPortfolio(CreatePortfolioDTO dto, User user);
} 
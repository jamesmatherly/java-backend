package com.example.javabackend.service;

import com.example.javabackend.model.Portfolio;
import java.util.List;

public interface PortfolioService extends BaseService<Portfolio, Long> {
    List<Portfolio> findByUserId(String userId);
} 
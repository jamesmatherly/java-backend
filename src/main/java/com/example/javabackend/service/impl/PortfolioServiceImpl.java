package com.example.javabackend.service.impl;

import com.example.javabackend.model.Portfolio;
import com.example.javabackend.repository.PortfolioRepository;
import com.example.javabackend.service.PortfolioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PortfolioServiceImpl extends BaseServiceImpl<Portfolio, Long, PortfolioRepository> implements PortfolioService {
    
    public PortfolioServiceImpl(PortfolioRepository repository) {
        super(repository);
    }

    @Override
    public List<Portfolio> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }
} 
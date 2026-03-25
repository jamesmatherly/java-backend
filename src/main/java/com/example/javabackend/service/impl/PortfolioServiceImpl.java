package com.example.javabackend.service.impl;

import com.example.javabackend.dto.CreatePortfolioDTO;
import com.example.javabackend.mapper.PortfolioMapper;
import com.example.javabackend.model.Portfolio;
import com.example.javabackend.model.User;
import com.example.javabackend.repository.PortfolioRepository;
import com.example.javabackend.service.PortfolioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PortfolioServiceImpl extends BaseServiceImpl<Portfolio, String, PortfolioRepository> implements PortfolioService {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    public PortfolioServiceImpl(PortfolioRepository repository) {
        super(repository);
    }

    @Override
    public List<Portfolio> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public boolean createPortfolio(CreatePortfolioDTO dto, User user) {
        PortfolioMapper mapper = new PortfolioMapper();
        Portfolio portfolio = mapper.createDtoToPortfolio(dto);
        portfolio.setUser(user);
        portfolio.getPositions().get(0).setUser(user);
        try {
            repository.save(portfolio);
        } catch (Exception e) {
            logger.error("Could not create portfolio", e);
            return false;
        }
        return true;
    }
} 
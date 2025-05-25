package com.example.javabackend.controller;

import com.example.javabackend.model.Portfolio;
import com.example.javabackend.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController extends BaseController<Portfolio, Long> {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        super(portfolioService);
        this.portfolioService = portfolioService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Portfolio>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(portfolioService.findByUserId(userId));
    }
} 
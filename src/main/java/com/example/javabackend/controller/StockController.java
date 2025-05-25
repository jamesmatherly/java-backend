package com.example.javabackend.controller;

import com.example.javabackend.dto.StockDTO;
import com.example.javabackend.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockDTO> getStockInfo(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockInfo(symbol));
    }
} 
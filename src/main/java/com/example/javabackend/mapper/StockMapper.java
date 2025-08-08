package com.example.javabackend.mapper;

import com.example.javabackend.dto.FinnHubQuoteResponse;
import com.example.javabackend.dto.StockDTO;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {
    
    public StockDTO toStockDTO(FinnHubQuoteResponse quote, String symbol) {
        if (quote == null) {
            return null;
        }

        StockDTO stock = new StockDTO();
        stock.setSymbol(symbol);
        stock.setName(symbol); // You might want to fetch company name from another endpoint
        stock.setPrice(quote.getCurrentPrice());
        stock.setChange(quote.getChange());
        stock.setChangePercent(quote.getChangePercent());
        stock.setHigh(quote.getHigh());
        stock.setLow(quote.getLow());
        stock.setOpen(quote.getOpen());
        stock.setPreviousClose(quote.getPreviousClose());
        
        return stock;
    }
} 
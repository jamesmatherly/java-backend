package com.example.javabackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@Entity
@Table(name = "positions")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    @JsonBackReference
    private Portfolio portfolio;

    @Column(name = "stock_symbol")
    private String stockSymbol;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "average_price", nullable = false)
    private BigDecimal averagePrice;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "total_value")
    private BigDecimal totalValue;
} 
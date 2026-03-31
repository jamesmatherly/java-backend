package com.example.javabackend.controller;

import com.example.javabackend.dto.CreatePortfolioDTO;
import com.example.javabackend.dto.GetPortfoliosDTO;
import com.example.javabackend.dto.ResponseDTO;
import com.example.javabackend.mapper.PortfolioMapper;
import com.example.javabackend.model.Portfolio;
import com.example.javabackend.model.User;
import com.example.javabackend.service.PortfolioService;
import com.example.javabackend.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController extends BaseController<Portfolio, String> {
    private final PortfolioService portfolioService;
    private final UserService userService;

    public PortfolioController(
        PortfolioService portfolioService,
        UserService userService
    ) {
        super(portfolioService);
        this.portfolioService = portfolioService;
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<ResponseDTO<GetPortfoliosDTO>> getForCurrentUser(@AuthenticationPrincipal String cognitoId) {
        PortfolioMapper mapper = new PortfolioMapper();
        ResponseDTO<GetPortfoliosDTO> r = new ResponseDTO<>();
        r.setDataList(portfolioService.findByUserId(cognitoId).stream().map(p -> mapper.toPortfoliosDTO(p)).toList());
        r.setCount(r.getDataList().size());
        return ResponseEntity.ok(r);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Portfolio>> createPortfolio(
        @RequestBody CreatePortfolioDTO dto,
        @AuthenticationPrincipal String cognitoId
    ) {
        Optional<User> user = userService.findById(cognitoId);
        ResponseDTO<Portfolio> r = new ResponseDTO<>();
        if (user.isEmpty()) {
            r.setErrorMessage("User not found");
        } else {
            if (!portfolioService.createPortfolio(dto, user.get())) {
                r.setSuccess(false);
                r.setErrorMessage("Could not create portfolio");
            }   
            r.setDataList(portfolioService.findByUserId(user.get().getId()));
            r.setCount(r.getDataList().size());
        }

        return ResponseEntity.ok(r);
    }
} 
package com.example.javabackend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.javabackend.dto.ResponseDTO;
import com.example.javabackend.dto.TransactionDto;
import com.example.javabackend.model.Position;
import com.example.javabackend.service.PositionService;
import com.example.javabackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/positions")
public class PositionController extends BaseController<Position, String> {
    private final PositionService positionService;

    public PositionController(
        PositionService positionService,
        UserService userService
    ) {
        super(positionService);
        this.positionService = positionService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<Position>> getForCurrentUser(@RequestParam Map<String, String> params, @AuthenticationPrincipal String cognitoId) {
        ResponseDTO<Position> r = new ResponseDTO<>();
        if (params.containsKey("portfolioId")) {
            r.setDataList(positionService.findByPortfolioIdAndUserId(params.get("portfolioId"), cognitoId));
        }else if (params.containsKey("portfolioId")) {
            r.setDataList(positionService.findByUserId(cognitoId));
        }
        r.setCount(r.getDataList().size());
        return ResponseEntity.ok(r);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Position>> executeTransaction(@RequestBody TransactionDto dto, @AuthenticationPrincipal String cognitoId) {
        ResponseDTO<Position> r = new ResponseDTO<>();
        switch (dto.getTransactionType()) {
            case BUY_TO_OPEN  -> positionService.buyToOpen(dto);
            case BUY_TO_CLOSE -> positionService.buyToClose(dto);
            case SELL_TO_OPEN  -> positionService.sellToOpen(dto);
            case SELL_TO_CLOSE -> positionService.sellToClose(dto);
        }
        r.setDataList(positionService.findByPortfolioId(dto.getPortfolioId()));
        r.setCount(r.getDataList().size());
        return ResponseEntity.ok(r);
    }
    
}

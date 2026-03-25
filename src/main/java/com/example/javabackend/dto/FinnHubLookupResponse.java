package com.example.javabackend.dto;

import lombok.Data;

@Data
public class FinnHubLookupResponse {
    private int count;
    
    private Lookup[] result;

    @Data
    public static class Lookup {
        String description;
        String symbol;
        String displaySymbol;
        String type;
    }
} 
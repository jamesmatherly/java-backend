package com.example.javabackend.dto;

import java.util.List;

import lombok.Data;

@Data
public class ResponseDTO<T> {
    private boolean success = true;
    private int count;
    private List<T> dataList;
    private String errorMessage;
}

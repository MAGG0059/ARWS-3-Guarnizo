package edu.eci.arsw.blueprints.dtos;

public record ApiResponse<T>(
        int code,
        String message,
        T data
) {}
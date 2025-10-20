package com.frauddetector.orchestrator.dto;

import org.springframework.lang.NonNull;

public record TransactionDTO(
        String userId,
        Double value
) {
    @NonNull
    @Override
    public String toString() {
        return "TransactionDTO{" +
                "userId='" + userId + '\'' +
                ", value=" + value +
                '}';
    }
}
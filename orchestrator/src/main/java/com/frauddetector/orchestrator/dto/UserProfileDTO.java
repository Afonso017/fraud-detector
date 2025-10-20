package com.frauddetector.orchestrator.dto;

import org.springframework.lang.NonNull;

public record UserProfileDTO(
        String userId,
        int transactionCount,
        double averageAmount,
        String lastTransactionCountry
) {
    @NonNull
    @Override
    public String toString() {
        return "UserProfileDTO{" +
                "userId='" + userId + '\'' +
                ", transactionCount=" + transactionCount +
                ", averageAmount=" + averageAmount +
                ", lastTransactionCountry='" + lastTransactionCountry + '\'' +
                '}';
    }
}
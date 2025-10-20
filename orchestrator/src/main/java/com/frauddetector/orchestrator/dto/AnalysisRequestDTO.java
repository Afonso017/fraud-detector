package com.frauddetector.orchestrator.dto;

import org.springframework.lang.NonNull;

public record AnalysisRequestDTO(
        String userId,
        Double value,
        int transactionCount,
        double averageAmount,
        String lastTransactionCountry
) {
    @NonNull
    @Override
    public String toString() {
        return "AnalysisRequestDTO{" +
                "userId='" + userId + '\'' +
                ", value=" + value +
                ", transactionCount=" + transactionCount +
                ", averageAmount=" + averageAmount +
                ", lastTransactionCountry='" + lastTransactionCountry + '\'' +
                '}';
    }
}

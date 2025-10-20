package com.frauddetector.orchestrator.dto;

import org.springframework.lang.NonNull;

public record AnalysisResponseDTO(
        double riskScore,
        String recommendedAction
) {
    @NonNull
    @Override
    public  String toString() {
        return "AnalysisResponseDTO{" +
                "riskScore=" + riskScore +
                ", recommendedAction='" + recommendedAction + '\'' +
                '}';
    }
}
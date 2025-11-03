package com.frauddetector.auditservice.dto;

public record AnalysisResponseDTO(
        double riskScore,
        String recommendedAction
) {}

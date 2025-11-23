package com.frauddetector.orchestrator.dto;

public record AuditLogEvent(
    String userId,
    Double value,
    String country,
    String status,
    AnalysisResponseDTO riskAnalysis
) {}

package com.frauddetector.profile.dto;

public record AuditLogEvent(
    String userId,
    Double value,
    String status,
    AnalysisResponseDTO riskAnalysis
) {}

package com.frauddetector.orchestrator.dto;

public record AuditLogEvent(
    String status,
    AnalysisResponseDTO riskAnalysis
) {}

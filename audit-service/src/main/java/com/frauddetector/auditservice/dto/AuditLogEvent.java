package com.frauddetector.auditservice.dto;

public record AuditLogEvent(
        String status,
        AnalysisResponseDTO riskAnalysis
) {}

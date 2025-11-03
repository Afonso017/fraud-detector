package com.frauddetector.auditservice.service;

import com.frauddetector.auditservice.dto.AuditLogEvent;
import com.frauddetector.auditservice.entity.AuditLog;
import com.frauddetector.auditservice.repository.AuditLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditConsumer {

    private final AuditLogRepository repository;

    public AuditConsumer(AuditLogRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "fraud_analysis_events", groupId = "audit_group")
    public void consume(AuditLogEvent event) {
        System.out.println("<<< Evento de auditoria recebido: " + event);

        AuditLog log = new AuditLog();
        log.setStatus(event.status());
        log.setRiskScore(event.riskAnalysis().riskScore());
        log.setRecommendedAction(event.riskAnalysis().recommendedAction());
        log.setTimestamp(Instant.now());

        repository.save(log);
        System.out.println("<<< Evento salvo no banco de dados de auditoria.");
    }
}

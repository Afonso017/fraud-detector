package com.frauddetector.auditservice.service;

import com.frauddetector.auditservice.dto.AuditLogEvent;
import com.frauddetector.auditservice.entity.AuditLog;
import com.frauddetector.auditservice.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditConsumer {

    private final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    private final AuditLogRepository repository;

    public AuditConsumer(AuditLogRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "fraud_analysis_events", groupId = "audit_group")
    public void consume(AuditLogEvent event) {
        log.info("<<< Evento de auditoria recebido: {}", event);

        AuditLog auditLog = new AuditLog();
        auditLog.setStatus(event.status());
        auditLog.setRiskScore(event.riskAnalysis().riskScore());
        auditLog.setRecommendedAction(event.riskAnalysis().recommendedAction());
        auditLog.setTimestamp(Instant.now());

        repository.save(auditLog);
        log.info("<<< Evento salvo no banco de dados de auditoria.");
    }
}

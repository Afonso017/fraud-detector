package com.frauddetector.orchestrator.service;

import com.frauddetector.orchestrator.dto.AuditLogEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "fraud_analysis_events";
    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, AuditLogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAuditEvent(AuditLogEvent event) {
        // Envia a mensagem de forma assíncrona
        kafkaTemplate.send(TOPIC, event);
        System.out.println(">>> Evento de auditoria enviado para o Kafka: " + event);
    }
}

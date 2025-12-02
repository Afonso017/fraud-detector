package com.frauddetector.orchestrator.service;

import com.frauddetector.orchestrator.dto.AuditLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por enviar eventos de auditoria para o Kafka.
 */
@Service
public class KafkaProducerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TOPIC = "fraud_analysis_events";
    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, AuditLogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAuditEvent(AuditLogEvent event) {
        // Envia a mensagem de forma assíncrona
        kafkaTemplate.send(TOPIC, event);
        logger.info(">>> Evento de auditoria enviado para o Kafka: {}", event);
    }
}

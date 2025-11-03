package com.frauddetector.orchestrator.controller;

import com.frauddetector.orchestrator.dto.*;
import com.frauddetector.orchestrator.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/analyze")
public class FraudController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WebClient profileWebClient;
    private final WebClient inferenceWebClient;
    private final KafkaProducerService kafkaProducer;

    public FraudController(
            WebClient.Builder webClientBuilder,
            KafkaProducerService kafkaProducer
    ) {
        this.profileWebClient = webClientBuilder.baseUrl("http://profile-service:8082").build();
        this.inferenceWebClient = webClientBuilder.baseUrl("http://inference-service:8083").build();
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping
    public Mono<Map<String, Object>> analyzeFraud(@RequestBody(required = false) TransactionDTO transaction) {
        logger.info(">>> Requisição recebida: {}", transaction);

        // Chama o serviço de perfil
        return this.profileWebClient.get()
            .uri("/profiles/{userId}", transaction.userId())
            .retrieve()
            .bodyToMono(UserProfileDTO.class)
            .flatMap(userProfile -> {
                logger.info(">>> Perfil recebido: {}", userProfile);

                // Prepara o corpo da requisição para o serviço de inferência
                AnalysisRequestDTO analysisRequest = new AnalysisRequestDTO(
                    transaction.userId(),
                    transaction.value(),
                    userProfile.transactionCount(),
                    userProfile.averageAmount(),
                    userProfile.lastTransactionCountry()
                );

                // Chama o serviço de inferência com os dados enriquecidos
                return this.inferenceWebClient.post()
                    .uri("/predict")
                    .bodyValue(analysisRequest)
                    .retrieve()
                    .bodyToMono(AnalysisResponseDTO.class)
                    .map(analysisResponse -> {
                        logger.info(">>> Inferência recebida: {}", analysisResponse);
                        String action;
                        switch (analysisResponse.recommendedAction()) {
                            case "APPROVE" -> action = "Transação aprovada.";
                            case "DECLINE" -> action = "Transação rejeitada.";
                            case "REVIEW" -> action = "Transação em revisão.";
                            default -> action = "Ação desconhecida.";
                        }
                        logger.info(">>> Resultado: {}", action);

                        // Monta e retorna a resposta final
                        return Map.of(
                            "status", "ANALYSIS_COMPLETE",
                            "riskAnalysis", analysisResponse
                        );
                    })
                    // Envia o evento de auditoria de forma assíncrona
                    .doOnSuccess(responseMap -> {
                        AuditLogEvent event = new AuditLogEvent(
                                (String) responseMap.get("status"),
                                (AnalysisResponseDTO) responseMap.get("riskAnalysis")
                        );
                        kafkaProducer.sendAuditEvent(event);
                    });
            });
    }
}
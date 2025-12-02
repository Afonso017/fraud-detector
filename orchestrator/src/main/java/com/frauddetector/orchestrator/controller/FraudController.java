package com.frauddetector.orchestrator.controller;

import com.frauddetector.orchestrator.dto.*;
import com.frauddetector.orchestrator.service.KafkaProducerService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import fraud_detection.FraudDetectionServiceGrpc;
import fraud_detection.FraudDetection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Controlador REST para análise de fraude.
 */
@RestController
@RequestMapping("/analyze")
public class FraudController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WebClient profileWebClient;
    private final KafkaProducerService kafkaProducer;

    // Injeção do stub gRPC para comunicação com o serviço de inferência
    @GrpcClient("inference-service")
    private FraudDetectionServiceGrpc.FraudDetectionServiceBlockingStub fraudStub;

    public FraudController(
            WebClient.Builder webClientBuilder,
            KafkaProducerService kafkaProducer
    ) {
        // WebClient com balanceador de carga para comunicação com o serviço de perfil
        this.profileWebClient = webClientBuilder.baseUrl("http://profile-service-cluster").build();
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping
    public Mono<Map<String, Object>> analyzeFraud(@RequestBody(required = false) TransactionDTO transaction) {
        logger.info(">>> Requisição recebida: {}", transaction);

        if (transaction == null) {
            return Mono.error(new IllegalArgumentException("Transação inválida"));
        }

        // Chamada ao serviço de perfil para obter dados do usuário
        return this.profileWebClient.get()
            .uri("/profiles/{userId}", transaction.userId())
            .retrieve()
            .bodyToMono(UserProfileDTO.class)
            .flatMap(userProfile -> {

                // Envolve a chamada bloqueante gRPC em um Mono.fromCallable
                return Mono.fromCallable(() -> {

                    // Prepara a requisição gRPC
                    FraudDetection.AnalysisRequest grpcRequest = FraudDetection.AnalysisRequest.newBuilder()
                        .setUserId(transaction.userId())
                        .setValue(transaction.value())
                        .setTransactionCount(userProfile.transactionCount())
                        .setAverageAmount(userProfile.averageAmount())
                        .setLastTransactionCountry(userProfile.lastTransactionCountry())
                        .setCurrentTransactionCountry(transaction.country() != null ? transaction.country() : "BRA")
                        .build();

                    // Chamada gRPC (bloqueante)
                    // Isso trava a thread até o Python responder
                    return fraudStub.predictFraud(grpcRequest);
                })
                // Move a execução do bloco acima para um pool de threads separado,
                // liberando o Event Loop do WebFlux para aceitar novas requisições imediatamente.
                .subscribeOn(Schedulers.boundedElastic())
                .map(grpcResponse -> {
                    // Mapeamento da resposta
                    AnalysisResponseDTO analysisResponse = new AnalysisResponseDTO(
                        grpcResponse.getRiskScore(),
                        grpcResponse.getRecommendedAction()
                    );

                    return Map.of(
                        "status", "ANALYSIS_COMPLETE",
                        "riskAnalysis", analysisResponse
                    );
                });
            })
            // Log de auditoria após o processamento (assíncrono)
            .doOnSuccess(responseMap -> {
                if (responseMap != null) {
                    AuditLogEvent event = new AuditLogEvent(
                        transaction.userId(),
                        transaction.value(),
                        transaction.country(),
                        (String) responseMap.get("status"),
                        (AnalysisResponseDTO) responseMap.get("riskAnalysis")
                    );
                    kafkaProducer.sendAuditEvent(event);
                }
            });
    }
}
package com.apigateway.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Rota para o serviço do orquestrador de fraude
            .route("fraud_analysis_route",
                r -> r.path("/analyze/**")
                    .uri("lb://orchestrator-cluster"))
            .build();
    }
}
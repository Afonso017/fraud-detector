package com.frauddetector.profile.service;

import com.frauddetector.profile.dto.AuditLogEvent;
import com.frauddetector.profile.entity.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProfileUpdaterListener {

    private final Logger logger = LoggerFactory.getLogger(ProfileUpdaterListener.class);

    private final UserProfileService profileService;

    public ProfileUpdaterListener(UserProfileService profileService) {
        this.profileService = profileService;
    }

    // Escuta o mesmo tópico que o orchestrator publica
    @KafkaListener(topics = "fraud_analysis_events", groupId = "profile_updater_group")
    public void handleProfileUpdate(AuditLogEvent event) {
        if (event.value() == null || event.value() <= 0) {
            logger.info("<<< Evento com valor negativo recebido. Ignorando para cálculo de média.");
            return;
        }

        logger.info("<<< Evento de transação recebido para: {}", event.userId());

        // Busca o perfil atual
        UserProfile profile = profileService.getProfile(event.userId());

        logger.info("<<< Perfil atual carregado para: {}", event.userId());

        // Atualiza o perfil com base na transação
        int newCount = profile.getTransactionCount() + 1;
        double newAverage = ((profile.getAverageAmount() * profile.getTransactionCount()) + event.value()) / newCount;

        profile.setTransactionCount(newCount);
        profile.setAverageAmount(newAverage);

        if (event.country() != null) {
            profile.setLastTransactionCountry(event.country());
        }

        // Salva o perfil atualizado de volta no Redis
        profileService.saveProfile(profile);
        logger.info("<<< Perfil atualizado e salvo no banco: {}", profile);
    }
}

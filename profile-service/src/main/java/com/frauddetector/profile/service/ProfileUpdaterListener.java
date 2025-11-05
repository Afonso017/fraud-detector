package com.frauddetector.profile.service;

import com.frauddetector.profile.dto.AuditLogEvent;
import com.frauddetector.profile.entity.UserProfile;
import com.frauddetector.profile.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProfileUpdaterListener {

    private final Logger logger = LoggerFactory.getLogger(ProfileUpdaterListener.class);

    private final UserProfileRepository repository;

    public ProfileUpdaterListener(UserProfileRepository repository) {
        this.repository = repository;
    }

    // Escuta o mesmo tópico que o orchestrator publica
    @KafkaListener(topics = "fraud_analysis_events", groupId = "profile_updater_group")
    public void handleProfileUpdate(AuditLogEvent event) {
        logger.info("<<< Evento de transação recebido para: {}", event.userId());

        // Busca o perfil atual
        UserProfile profile = repository.findById(event.userId())
                .orElseGet(() -> createDefaultProfile(event.userId()));

        // Atualiza o perfil com base na transação
        int count = profile.getTransactionCount();
        double newAverage = ((profile.getAverageAmount() * count) + event.value()) / (count - 1);

        profile.setTransactionCount(count);
        profile.setAverageAmount(newAverage);

        // Salva o perfil atualizado de volta no Redis
        repository.save(profile);
        logger.info("<<< Perfil atualizado e salvo para: {}", event.userId());
    }

    /**
     * Cria um objeto UserProfile padrão em memória para um novo usuário.
     */
    private UserProfile createDefaultProfile(String userId) {
        UserProfile defaultProfile = new UserProfile();
        defaultProfile.setUserId(userId);
        defaultProfile.setTransactionCount(0);
        defaultProfile.setAverageAmount(0.0);
        defaultProfile.setLastTransactionCountry("N/A");
        return defaultProfile;
    }
}

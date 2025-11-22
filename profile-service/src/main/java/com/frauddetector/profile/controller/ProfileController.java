package com.frauddetector.profile.controller;

import com.frauddetector.profile.entity.UserProfile;
import com.frauddetector.profile.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserProfileRepository userProfileRepository;

    public ProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String userId) {
        //logger.info(">>> Buscando perfil no DB para o usuário: {}", userId);

        // Tenta encontrar o usuário no banco pelo ID
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> createDefaultProfile(userId));

        // Retorna o perfil encontrado ou o padrão
        return ResponseEntity.ok(profile);
    }

    /**
     * Cria um objeto UserProfile padrão em memória para um novo usuário.
     * Não salva no banco de dados, mantendo a operação rápida.
     */
    private UserProfile createDefaultProfile(String userId) {
        //logger.info(">>> Histórico de usuário não encontrado. Criando perfil padrão para {}", userId);
        UserProfile defaultProfile = new UserProfile();
        defaultProfile.setUserId(userId);
        defaultProfile.setTransactionCount(0);              // Novo usuário tem 0 transações
        defaultProfile.setAverageAmount(0.0);               // Novo usuário tem 0 gasto médio
        defaultProfile.setLastTransactionCountry("N/A");    // País desconhecido

        return defaultProfile;
    }
}
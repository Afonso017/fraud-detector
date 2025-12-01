package com.frauddetector.profile.service;

import com.frauddetector.profile.entity.UserProfile;
import com.frauddetector.profile.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);
    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Se o perfil estiver na RAM, retorna-o instantaneamente. <br>
     * Se não, busca no Redis, guarda na RAM e retorna.
     */
    @Cacheable(value = "profiles", key = "#userId")
    public UserProfile getProfile(String userId) {
        log.info(">>> Cache Miss: A buscar perfil ao Redis para: {}", userId);
        return repository.findById(userId)
                .orElseGet(() -> createDefaultProfile(userId));
    }

    /**
     * Salva o perfil no Redis e atualiza a cache na RAM.
     */
    @CachePut(value = "profiles", key = "#profile.getUserId()")
    public UserProfile saveProfile(UserProfile profile) {
        log.info(">>> Cache Put: A atualizar perfil na RAM e Redis para: {}", profile.getUserId());
        return repository.save(profile);
    }

    private UserProfile createDefaultProfile(String userId) {
        UserProfile defaultProfile = new UserProfile();
        defaultProfile.setUserId(userId);
        defaultProfile.setTransactionCount(0);
        defaultProfile.setAverageAmount(0.0);
        defaultProfile.setLastTransactionCountry("N/A");
        return defaultProfile;
    }
}

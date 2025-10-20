package com.frauddetector.profile.controller;

import com.frauddetector.profile.entity.UserProfile;
import com.frauddetector.profile.repository.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final UserProfileRepository userProfileRepository;

    public ProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String userId) {
        System.out.println(">>> Buscando perfil no DB para o usuário: " + userId);

        // Busca o perfil no banco de dados pelo ID
        return userProfileRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
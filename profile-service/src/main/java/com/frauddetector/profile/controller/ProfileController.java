package com.frauddetector.profile.controller;

import com.frauddetector.profile.entity.UserProfile;
import com.frauddetector.profile.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final UserProfileService service;

    public ProfileController(UserProfileService service) {
        this.service = service;
    }

    /**
     * Obtém o perfil do utilizador pelo ID.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String userId) {
        UserProfile profile = service.getProfile(userId);
        return ResponseEntity.ok(profile);
    }
}

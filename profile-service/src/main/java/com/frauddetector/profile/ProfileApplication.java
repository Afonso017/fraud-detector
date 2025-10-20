package com.frauddetector.profile;

import com.frauddetector.profile.entity.UserProfile;
import com.frauddetector.profile.repository.UserProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileApplication.class, args);
    }

    // Dados mockados para teste
    @Bean
    CommandLineRunner initDatabase(UserProfileRepository repository) {
        return args -> {
            UserProfile testUser = new UserProfile();
            testUser.setUserId("user123");
            testUser.setTransactionCount(127);
            testUser.setAverageAmount(75.50);
            testUser.setLastTransactionCountry("BRA");
            repository.save(testUser);
            System.out.println(">>> Usuário de teste 'user123' inserido no banco de dados.");
        };
    }
}
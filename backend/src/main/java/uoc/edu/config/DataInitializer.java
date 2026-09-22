package uoc.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import uoc.edu.model.Role;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;

import java.time.LocalDateTime;

//DataInitializer serves to provide an initial administrator
@Configuration
public class DataInitializer {

    @Value("${ADMIN_NAME}")
    private String adminName;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    //CommandLineRunner serves to execute the code automatically when the app is started
    @Bean
    @Order(1)
    CommandLineRunner createInitialAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            //find the email, if its not present, we create the user, encode the password, its attributes and save it
            String normalizedEmail = adminEmail
                            .trim()
                            .toLowerCase();

            if (
                    userRepository
                            .findByEmailIgnoreCase(
                                    normalizedEmail)
                            .isPresent()
            ) {
                return;
            }

            User admin = new User();

            admin.setName(
                    adminName.trim()
            );

            admin.setEmail(
                    normalizedEmail
            );

            admin.setPasswordHash(
                    passwordEncoder.encode(adminPassword)
            );

            admin.setRole(
                    Role.ADMIN
            );

            admin.setCreatedAt(
                    LocalDateTime.now()
            );

            userRepository.save(admin);
        };
    }
}
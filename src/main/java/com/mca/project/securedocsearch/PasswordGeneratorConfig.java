package com.mca.project.securedocsearch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordGeneratorConfig {
    @Bean
    CommandLineRunner generatePassword() {
        return args -> {
            String rawPassword = "admin@123";
            String encoded = new BCryptPasswordEncoder().encode(rawPassword);
            System.out.println("BCrypt hash for admin@123 = " + encoded);
        };
    }
}

package com.xml.processor.config;

import com.xml.processor.model.User;
import com.xml.processor.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                // Create admin user
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@example.com");
                adminUser.setPassword(passwordEncoder.encode("admin"));
                Set<String> adminRoles = new HashSet<>();
                adminRoles.add("ADMIN");
                adminRoles.add("USER");
                adminUser.setRoles(adminRoles);
                adminUser.setEnabled(true);
                adminUser.setAccountLocked(false);
                userRepository.save(adminUser);

                // Create regular user
                User user = new User();
                user.setUsername("user");
                user.setEmail("user@example.com");
                user.setPassword(passwordEncoder.encode("user"));
                Set<String> userRoles = new HashSet<>();
                userRoles.add("USER");
                user.setRoles(userRoles);
                user.setEnabled(true);
                user.setAccountLocked(false);
                userRepository.save(user);
            }
        };
    }
} 
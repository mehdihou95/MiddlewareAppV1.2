package com.xml.processor.config;

import com.xml.processor.model.User;
import com.xml.processor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create admin user with ADMIN role
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setEmail("admin@example.com");
            Set<String> adminRoles = new HashSet<>();
            adminRoles.add("ADMIN");
            adminUser.setRoles(adminRoles);
            userRepository.save(adminUser);
            logger.info("Created admin user with ADMIN role");
        }
        
        // Create regular user with USER role
        if (!userRepository.existsByUsername("user")) {
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setPassword(passwordEncoder.encode("user"));
            regularUser.setEmail("user@example.com");
            Set<String> userRoles = new HashSet<>();
            userRoles.add("USER");
            regularUser.setRoles(userRoles);
            userRepository.save(regularUser);
            logger.info("Created regular user with USER role");
        }
    }
} 
package com.xml.processor.controller;

import com.xml.processor.security.RateLimiter;
import com.xml.processor.security.service.CsrfTokenService;
import com.xml.processor.security.service.JwtService;
import com.xml.processor.security.service.impl.InMemoryJwtBlacklistService;
import com.xml.processor.service.impl.SecurityLoggerServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@TestConfiguration
@EnableWebSecurity
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthControllerTestConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        UserDetails testUser = User.builder()
            .username("testuser")
            .password(passwordEncoder().encode("testpass"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(testUser);
    }

    @Bean
    @Primary
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return authentication -> authenticationProvider.authenticate(authentication);
    }

    @Bean
    @Primary
    public JwtService jwtService() {
        return new JwtService();
    }

    @Bean
    @Primary
    public InMemoryJwtBlacklistService jwtBlacklistService() {
        return new InMemoryJwtBlacklistService();
    }

    @Bean
    @Primary
    public RateLimiter rateLimiter() {
        return new RateLimiter();
    }

    @Bean
    @Primary
    public SecurityLoggerServiceImpl securityLoggerService() {
        return new SecurityLoggerServiceImpl();
    }

    @Bean
    @Primary
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    @Primary
    public CsrfTokenService csrfTokenService(CsrfTokenRepository csrfTokenRepository) {
        return new CsrfTokenService(csrfTokenRepository);
    }
} 
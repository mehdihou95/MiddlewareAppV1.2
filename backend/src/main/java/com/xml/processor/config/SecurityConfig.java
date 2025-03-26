package com.xml.processor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import jakarta.servlet.http.Cookie;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/");
        tokenRepository.setCookieName("XSRF-TOKEN");
        tokenRepository.setHeaderName("X-XSRF-TOKEN");

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(tokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers(
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/refresh-csrf",
                    "/api/auth/validate",
                    "/h2-console/**",
                    "/error",
                    "/favicon.ico"
                )
            )
            .addFilterBefore(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (csrf != null) {
                        String token = csrf.getToken();
                        logger.info("CSRF Token in filter: " + token);
                        response.setHeader(csrf.getHeaderName(), token);
                        Cookie cookie = new Cookie("XSRF-TOKEN", token);
                        cookie.setPath("/");
                        cookie.setSecure(request.isSecure());
                        cookie.setHttpOnly(false);
                        cookie.setMaxAge(3600); // 1 hour
                        response.addCookie(cookie);
                    } else {
                        logger.warn("No CSRF token found in request");
                    }
                    filterChain.doFilter(request, response);
                }
            }, CsrfFilter.class)
            .addFilterAfter(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    if (!request.getMethod().equals("GET")) {
                        String headerToken = request.getHeader("X-XSRF-TOKEN");
                        logger.info("CSRF Token in request header: " + (headerToken != null ? headerToken : "null"));
                        
                        String cookieToken = null;
                        Cookie[] cookies = request.getCookies();
                        if (cookies != null) {
                            for (Cookie cookie : cookies) {
                                if ("XSRF-TOKEN".equals(cookie.getName())) {
                                    cookieToken = cookie.getValue();
                                    break;
                                }
                            }
                        }
                        logger.info("CSRF Token in cookie: " + (cookieToken != null ? cookieToken : "null"));
                    }
                    filterChain.doFilter(request, response);
                }
            }, CsrfFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/api/clients/**").authenticated()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("frame-ancestors 'self'")
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<AuthorizationLoggingFilter> authorizationLoggingFilter() {
        FilterRegistrationBean<AuthorizationLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthorizationLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<CsrfTokenLoggingFilter> csrfTokenLoggingFilter() {
        FilterRegistrationBean<CsrfTokenLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CsrfTokenLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-XSRF-TOKEN",
            "X-Requested-With",
            "Accept",
            "Origin"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

class CsrfTokenGenerationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenGenerationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            logger.debug("Processing CSRF token for request: {}", request.getRequestURI());
            
            // Set the CSRF token in the response header
            response.setHeader(csrf.getHeaderName(), csrf.getToken());
            
            // Set the CSRF token as a cookie
            Cookie cookie = new Cookie("XSRF-TOKEN", csrf.getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(false); // Allow JavaScript access
            cookie.setSecure(request.isSecure()); // Set secure flag if using HTTPS
            response.addCookie(cookie);
            
            logger.debug("CSRF token set in header {} and cookie: {}", csrf.getHeaderName(), csrf.getToken());
        } else {
            logger.debug("No CSRF token found for request: {}", request.getRequestURI());
        }
        
        filterChain.doFilter(request, response);
    }
}

class AuthorizationLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationLoggingFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        logger.debug("Request to {}: Authentication = {}, Authorities = {}", 
            request.getRequestURI(),
            auth != null ? auth.getName() : "null",
            auth != null ? auth.getAuthorities() : "null");
        
        filterChain.doFilter(request, response);
        
        logger.debug("Response from {}: Status = {}", 
            request.getRequestURI(), 
            response.getStatus());
    }
}

class CsrfTokenLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenLoggingFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        String csrfHeader = request.getHeader("X-XSRF-TOKEN");
        String csrfCookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
            .filter(cookie -> "XSRF-TOKEN".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
            
        logger.debug("CSRF Debug - URI: {}, Method: {}, Header Token: {}, Cookie Token: {}", 
            request.getRequestURI(),
            request.getMethod(),
            csrfHeader,
            csrfCookie);
            
        filterChain.doFilter(request, response);
        
        Collection<String> responseHeaders = response.getHeaderNames();
        logger.debug("Response Headers for {}: {}", request.getRequestURI(), responseHeaders);
    }
}
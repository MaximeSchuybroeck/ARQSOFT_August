package com.example.gateway_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui/**","/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/book-service/**").permitAll()   // allow discovery path
                        .pathMatchers("/api/auth/**").permitAll()
                        .anyExchange().authenticated()
                )


                .build();
    }
}


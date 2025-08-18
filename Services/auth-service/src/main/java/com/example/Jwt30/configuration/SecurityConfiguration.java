package com.example.Jwt30.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * Password encoder that can also be injected in your services.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Main security configuration:
     * - CSRF enabled, but ignored for /api/** (REST/JWT calls)
     * - /login (GET & POST form), /oauth2/**, /signup, /api/login, static files are allowed
     * - Form login + OAuth2 redirect to /landing
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CORS with defaults; adjust if you have a separate frontend on another origin
                .cors(Customizer.withDefaults())

                // CSRF: keep enabled for HTML forms; disable for REST APIs
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/**"           // JWT/REST endpoints
                        )
                )

                // Access rules
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible endpoints:
                        .requestMatchers(
                                "/",                 // optional home
                                "/login",            // login page (GET) + form processing (POST)
                                "/oauth2/**",        // OAuth2 flows
                                "/signup",           // signup page/endpoint if present
                                "/api/login",        // your REST/JWT login endpoint
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/h2-console/**"     // only for dev; remove in production
                        ).permitAll()
                        // everything else requires authentication
                        .anyRequest().authenticated()
                )

                // H2 console support (allow frames)
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // Sessions: required for form-login and oauth2; IF_REQUIRED works fine with JWT for APIs
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // Form login via your login.html (GET /login). Form POST goes to /login (loginProcessingUrl)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")   // <form method="post" action="/login">
                        .defaultSuccessUrl("/landing", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // OAuth2 login (Google/Facebook). After success also redirect to /landing
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/landing", true)
                )
                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        // If you have a custom JWT filter for API calls, add it here:
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

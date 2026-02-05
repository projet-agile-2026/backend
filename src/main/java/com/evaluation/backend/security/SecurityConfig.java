package com.evaluation.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactive le CSRF pour les tests d'API (Postman)
            .csrf(csrf -> csrf.disable())
            // Autorise toutes les requêtes sans authentification
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Désactive les formulaires de login et l'auth basic
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
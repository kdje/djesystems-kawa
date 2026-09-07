package com.djesystems.kawa.retailer.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                /*
                 * Important :
                 * permet à Spring de retourner correctement
                 * les erreurs 400 / 404 / 409...
                 */
                .dispatcherTypeMatchers(
                    DispatcherType.ERROR
                ).permitAll()

                .requestMatchers(
                    "/error",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()

                /*
                 * TEMPORAIREMENT ouvert en développement local.
                 */
                .requestMatchers(
                    "/api/admin/**"
                ).permitAll()

                .anyRequest().denyAll()
            );

        return http.build();
    }
}
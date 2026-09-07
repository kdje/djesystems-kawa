package com.djesystems.kawa.wallet.config;

import com.djesystems.kawa.wallet.security.AudienceValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            );

        return http.build();
    }

    /**
     * Décode et valide les JWT Microsoft Entra ID.
     *
     * Validation :
     * - signature Microsoft
     * - expiration
     * - issuer
     * - audience KAWA Wallet Service DEV
     */
    @Bean
    JwtDecoder jwtDecoder(KawaSecurityProperties properties) {

        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withIssuerLocation(
                    properties.getIssuerUri()
                )
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
            JwtValidators.createDefaultWithIssuer(
                properties.getIssuerUri()
            );

        OAuth2TokenValidator<Jwt> audienceValidator =
            new AudienceValidator(
                properties.getAudience()
            );

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                audienceValidator
            )
        );

        return decoder;
    }
}
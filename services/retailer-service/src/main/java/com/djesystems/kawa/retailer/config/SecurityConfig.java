package com.djesystems.kawa.retailer.config;

import com.djesystems.kawa.retailer.security.AudienceValidator;
import com.djesystems.kawa.retailer.security.FirebaseAuthenticationFilter;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain retailerPortalSecurityFilterChain(
            HttpSecurity http,
            FirebaseAuth firebaseAuth,
            @Value("${kawa.retailer-portal.firebase.project-id}") String firebaseProjectId,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .securityMatcher("/api/retailer-portal/**")
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(
                    (request, response, exception) ->
                        response.sendError(
                            jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                new FirebaseAuthenticationFilter(firebaseAuth, firebaseProjectId),
                AnonymousAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                /*
                 * Permet à Spring de retourner correctement
                 * les erreurs applicatives 400 / 404 / 409...
                 */
                .dispatcherTypeMatchers(
                    DispatcherType.ERROR
                ).permitAll()

                /*
                 * Endpoints techniques non protégés.
                 */
                .requestMatchers(
                    "/error",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()

                /*
                 * Administration Retailer :
                 * JWT Entra obligatoire.
                 */
                .requestMatchers(
                    "/api/admin/**"
                ).authenticated()

                /*
                 * Tout le reste est interdit.
                 */
                .anyRequest().denyAll()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${kawa.retailer-portal.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
            Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/retailer-portal/**", configuration);
        return source;
    }

    /**
     * Décode et valide les JWT Microsoft Entra ID.
     *
     * Validation :
     * - signature Microsoft
     * - expiration
     * - issuer
     * - audience KAWA Retailer Service
     */
    @Bean
    JwtDecoder jwtDecoder(
            KawaSecurityProperties properties) {

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
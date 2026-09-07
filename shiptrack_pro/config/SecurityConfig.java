package com.shiptrack.shiptrack_pro.config;

import com.shiptrack.shiptrack_pro.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // ==========================================
                // CSRF
                // ==========================================
                .csrf(AbstractHttpConfigurer -> AbstractHttpConfigurer.disable())

                // ==========================================
                // SESSION MANAGEMENT
                // ==========================================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ==========================================
                // AUTHORIZATION
                // ==========================================
                .authorizeHttpRequests(auth -> auth

                        // ==========================================
                        // AUTHENTICATION
                        // ==========================================
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // ==========================================
                        // WEBSOCKET / SOCKJS
                        // ==========================================
                        .requestMatchers(
                                "/ws/**"
                        ).permitAll()

                        // ==========================================
                        // NOTIFICATIONS
                        // ==========================================
                        .requestMatchers(
                                "/api/notifications/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "BUSINESS_CLIENT",
                                "LOGISTICS_OPERATOR",
                                "SUPPORT_AGENT",
                                "ADMINISTRATOR"
                        )

                        // ==========================================
                        // SHIPMENT CREATION
                        // ==========================================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/shipments"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "BUSINESS_CLIENT"
                        )

                        // ==========================================
                        // LIVE ROUTE LOCATION
                        // ==========================================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/routes/*/location"
                        ).hasAnyRole(
                                "OPERATOR",
                                "LOGISTICS_OPERATOR",
                                "ADMINISTRATOR"
                        )

                        // ==========================================
                        // ROUTE MANAGEMENT
                        // ==========================================
                        .requestMatchers(
                                "/api/routes/**"
                        ).hasAnyRole(
                                "LOGISTICS_OPERATOR",
                                "ADMINISTRATOR"
                        )

                        // ==========================================
                        // TRACKING
                        // ==========================================
                        .requestMatchers(
                                "/api/tracking/**"
                        ).hasAnyRole(
                                "LOGISTICS_OPERATOR",
                                "ADMINISTRATOR"
                        )

                        // ==========================================
                        // POD - SUBMIT PROOF
                        // ==========================================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pod/**"
                        ).hasRole(
                                "LOGISTICS_OPERATOR"
                        )

                        // ==========================================
                        // POD - VERIFY / GET
                        // ==========================================
                        .requestMatchers(
                                "/api/pod/**"
                        ).authenticated()

                        // ==========================================
                        // ANALYTICS / REPORTS
                        // ==========================================
                        .requestMatchers(
                                "/api/analytics/**",
                                "/api/reports/**"
                        ).hasAnyRole(
                                "BUSINESS_CLIENT",
                                "ADMINISTRATOR"
                        )

                        // ==========================================
                        // ADMIN
                        // ==========================================
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole(
                                "ADMINISTRATOR"
                        )

                        // ==========================================
                        // EVERYTHING ELSE
                        // ==========================================
                        .anyRequest().authenticated()
                )

                // ==========================================
                // HTTP BASIC DISABLED
                // ==========================================
                .httpBasic(httpBasic ->
                        httpBasic.disable()
                )

                // ==========================================
                // FORM LOGIN DISABLED
                // ==========================================
                .formLogin(formLogin ->
                        formLogin.disable()
                )

                // ==========================================
                // JWT FILTER
                // ==========================================
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
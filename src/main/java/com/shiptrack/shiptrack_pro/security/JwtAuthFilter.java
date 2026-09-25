package com.shiptrack.shiptrack_pro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ==========================================================
        // GET AUTHORIZATION HEADER
        // ==========================================================

        final String authHeader =
                request.getHeader("Authorization");

        // No JWT token
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================================
        // EXTRACT TOKEN
        // ==========================================================

        final String token =
                authHeader.substring(7);

        try {

            // ======================================================
            // EXTRACT EMAIL FROM TOKEN
            // ======================================================

            String email =
                    jwtUtil.extractEmail(token);

            if (email == null || email.isBlank()) {

                log.warn(
                        "JWT token does not contain a valid email"
                );

                filterChain.doFilter(request, response);
                return;
            }

            // ======================================================
            // CHECK EXISTING AUTHENTICATION
            // ======================================================

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                // ==================================================
                // LOAD USER
                // ==================================================

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                // ==================================================
                // VALIDATE TOKEN
                // ==================================================

                if (jwtUtil.isTokenValid(
                        token,
                        userDetails.getUsername()
                )) {

                    // ==============================================
                    // CREATE AUTHENTICATION
                    // ==============================================

                    UsernamePasswordAuthenticationToken
                            authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // ==============================================
                    // SET SECURITY CONTEXT
                    // ==============================================

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );

                    // ==============================================
                    // LOG USER
                    // ==============================================

                    log.info(
                            "JWT authenticated user: {}",
                            email
                    );

                    log.info(
                            "User authorities: {}",
                            userDetails.getAuthorities()
                    );

                } else {

                    log.warn(
                            "Invalid JWT token for user: {}",
                            email
                    );
                }
            }

        } catch (Exception e) {

            log.error(
                    "JWT authentication failed: {}",
                    e.getMessage()
            );
        }

        // ==========================================================
        // CONTINUE FILTER CHAIN
        // ==========================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}
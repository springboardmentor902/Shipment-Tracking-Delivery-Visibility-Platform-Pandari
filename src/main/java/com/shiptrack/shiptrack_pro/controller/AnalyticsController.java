package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.AdminAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.BusinessAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.CustomerAnalyticsResponse;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/customer")
    public ResponseEntity<CustomerAnalyticsResponse> getCustomerAnalytics(Authentication authentication) {
        User user = getLoggedInUser(authentication);
        requireRole(user, "CUSTOMER");
        return ResponseEntity.ok(analyticsService.getCustomerAnalytics(user.getId()));
    }

    @GetMapping("/business")
    public ResponseEntity<BusinessAnalyticsResponse> getBusinessAnalytics(Authentication authentication) {
        User user = getLoggedInUser(authentication);
        requireRole(user, "BUSINESS_CLIENT");

        // In this project Shipment.businessId identifies the owning business.
        return ResponseEntity.ok(analyticsService.getBusinessAnalytics(user.getId()));
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminAnalyticsResponse> getAdminAnalytics(Authentication authentication) {
        User user = getLoggedInUser(authentication);
        requireRole(user, "ADMINISTRATOR");
        return ResponseEntity.ok(analyticsService.getAdminAnalytics());
    }

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Logged-in user not found"));
    }

    private void requireRole(User user, String requiredRole) {
        String role = user.getRole();
        if (role == null || !requiredRole.equalsIgnoreCase(role)
                && !requiredRole.equalsIgnoreCase(role.replace("ROLE_", ""))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied for this role");
        }
    }
}

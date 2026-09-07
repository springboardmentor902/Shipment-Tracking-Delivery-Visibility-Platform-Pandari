package com.shiptrack.shiptrack_pro.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private String firebaseCredentialsPath;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            try (FileInputStream serviceAccount =
                         new FileInputStream(firebaseCredentialsPath)) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(
                                GoogleCredentials.fromStream(serviceAccount)
                        )
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);

                log.info("Firebase App initialized successfully");

                return app;
            }

        } catch (IOException e) {
            log.error("Error initializing Firebase", e);
            throw new RuntimeException(
                    "Failed to initialize Firebase. Check the credentials path.",
                    e
            );
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
package com.djesystems.kawa.retailer.config;

import java.io.IOException;
import java.util.Optional;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetailerPortalFirebaseConfiguration {

    @Bean
    FirebaseApp retailerPortalFirebaseApp(
            @Value("${kawa.retailer-portal.firebase.project-id}") String projectId)
            throws IOException {
        String appName = "kawa-retailer-portal";
        Optional<FirebaseApp> existing = FirebaseApp.getApps().stream()
            .filter(app -> app.getName().equals(appName))
            .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        return FirebaseApp.initializeApp(
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(projectId)
                .build(),
            appName
        );
    }

    @Bean
    FirebaseAuth retailerPortalFirebaseAuth(FirebaseApp retailerPortalFirebaseApp) {
        return FirebaseAuth.getInstance(retailerPortalFirebaseApp);
    }
}

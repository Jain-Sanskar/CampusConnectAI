package com.campusconnect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the CampusConnect AI backend.
 *
 * CampusConnect AI is an AI-powered academic resource and guidance portal:
 * students authenticate, browse categorized resources, and chat with an
 * LLM-backed "AI Senior" mentor.
 */
@SpringBootApplication
@EnableScheduling
public class CampusConnectApplication {

    private static final Logger log = LoggerFactory.getLogger(CampusConnectApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CampusConnectApplication.class, args);
        log.info("CampusConnect AI backend started successfully.");
    }
}

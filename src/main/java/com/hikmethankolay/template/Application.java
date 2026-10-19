package com.hikmethankolay.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the application.
 *
 * <p>Component scanning starts from this package, so every feature package
 * ({@code auth}, {@code user}, ...) must live below it.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class Application {

    /**
     * Starts the Spring application context and the embedded web server.
     *
     * @param args command-line arguments, passed through to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

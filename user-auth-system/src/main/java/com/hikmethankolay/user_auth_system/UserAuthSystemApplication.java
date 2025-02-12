/**
 * @file UserAuthSystemApplication.java
 * @brief Main entry point for the User Authentication System application.
 *
 * This Spring Boot application serves as the starting point for the User Authentication System.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @brief Entry point for the User Authentication System Spring Boot application.
 */
@SpringBootApplication
public class UserAuthSystemApplication {

	/**
	 * Main method that launches the Spring Boot application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(UserAuthSystemApplication.class, args);
	}
}

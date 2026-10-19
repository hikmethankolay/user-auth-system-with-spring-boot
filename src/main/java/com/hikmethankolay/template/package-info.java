/**
 * Root package of the Spring Boot backend template.
 *
 * <p>Code is organised by feature rather than by layer:</p>
 * <ul>
 *   <li>{@code auth} - registration, login, token refresh and logout</li>
 *   <li>{@code user} - user entity, persistence, profile and administration endpoints</li>
 *   <li>{@code security} - Spring Security, JWT and brute-force protection</li>
 *   <li>{@code common} - cross-cutting error handling</li>
 *   <li>{@code config} - typed configuration and OpenAPI setup</li>
 * </ul>
 * Add new features as sibling packages following the same controller, service, repository and dto layout.
 */
package com.hikmethankolay.template;

package com.danielerikssoncoder.cinema_project.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security configuration for the entire application.
 * <p>
 * Defines which endpoints require which role (USER or ADMIN),
 * and tells Spring Security to use Keycloak JWT tokens for authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures security rules for all HTTP requests.
     * <p>
     * CSRF is disabled (not needed for stateless REST APIs).
     * Sessions are disabled (each request is authenticated via JWT).
     *
     * @param http  Spring's HTTP security builder
     * @return The configured security filter chain
     * @throws Exception  If configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Not needed for stateless REST APIs using JWT
                .csrf(csrf -> csrf.disable())

                // No session is stored. Every request must carry a valid JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Spring Boot Admin needs to reach /actuator without a JWT (also needed for our Dashboard)
                        .requestMatchers("/actuator/**").permitAll()

                        // USER and ADMIN can list movies and screenings
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/screenings").hasAnyRole("USER", "ADMIN")

                        // Booking flow for customers
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings").hasAnyRole("USER", "ADMIN")

                        // Ticket flow for customers
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets").hasAnyRole("USER", "ADMIN")

                        // Everything under /customers/** (including addresses) is ADMIN only
                        .requestMatchers("/api/v1/customers/**").hasRole("ADMIN")

                        // Only ADMIN can create or delete movies
                        .requestMatchers(HttpMethod.POST, "/api/v1/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/movies/**").hasRole("ADMIN")

                        // GET /movies/{id} must come after the DELETE rule but allow USER
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").hasAnyRole("USER", "ADMIN")

                        // Rooms are ADMIN only
                        .requestMatchers("/api/v1/rooms/**").hasRole("ADMIN")

                        // Only ADMIN can create or delete screenings
                        .requestMatchers(HttpMethod.POST, "/api/v1/screenings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/screenings/**").hasRole("ADMIN")

                        // Everything else requires at least a valid login
                        .anyRequest().authenticated()
                )

                // Configure Spring Security as an OAuth2 Resource Server validating JWTs
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /**
     * Creates the JWT converter that maps Keycloak roles to Spring Security authorities.
     * <p>
     * We use JwtAuthenticationConverter instead of a lambda-based @Bean because
     * a lambda conflicts with Spring's ConversionService and causes a startup error.
     *
     * @return  Configured JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /**
     * Extracts roles from Keycloak's realm_access claim and adds the ROLE_ prefix
     * that Spring Security requires for hasRole() to work.
     * <p>
     * Keycloak sends "USER", Spring expects "ROLE_USER".
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked") // JWT claims are Map<String, Object>, cast is apparently unavoidable
        public Collection<GrantedAuthority> convert(Jwt jwt) {

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            // Return empty list if realm_access is missing (for example: service account tokens)
            if (realmAccess == null || realmAccess.get("roles") == null) {
                return Collections.emptyList();
            }

            List<String> roles = (List<String>) realmAccess.get("roles");

            // "USER" becomes ROLE_USER, "ADMIN" becomes ROLE_ADMIN, etc.
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
    }
}

package com.danielerikssoncoder.cinema_project.service;

import com.danielerikssoncoder.cinema_project.entity.Customer;
import com.danielerikssoncoder.cinema_project.exception.ResourceNotFoundException;
import com.danielerikssoncoder.cinema_project.repository.CustomerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Helper service for authentication and ownership logic.
 * <p>
 * Kept separate from CustomerService to keep responsibilities clear:
 * CustomerService handles customer CRUD, AuthService answers
 * "who is logged in and do they own this resource?"
 */
@Service
public class AuthService {

    private final CustomerRepository customerRepository;

    public AuthService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Returns the Customer row for the currently logged-in user.
     * <p>
     * token.getToken().getSubject() extracts the sub claim from the JWT,
     * which is Keycloak's permanent unique ID for the user.
     * <p>
     * Throws 404 if no Customer row is linked to that Keycloak account,
     * for example: when admin1 calls a USER endpoint that requires a customer row.
     *
     * @param token  JWT token from the logged-in user
     * @return       The customer row linked to the token
     */
    public Customer getCurrentCustomer(JwtAuthenticationToken token) {
        String keycloakId = token.getToken().getSubject();
        return customerRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No customer linked to this account"));
    }

    /**
     * Verifies that the logged-in user owns the given resource.
     * <p>
     * Compares the sub claim in the JWT with the keycloakId of the resource owner.
     * <p>
     * Throws AccessDeniedException (caught as 403) if they do not match.
     * <p>
     * Used in BookingController to prevent customers from editing each other's bookings.
     *
     * @param resourceOwner The customer who owns the resource
     * @param token JWT token from the logged-in user
     */
    public void verifyOwnership(Customer resourceOwner, JwtAuthenticationToken token) {
        String keycloakId = token.getToken().getSubject();
        if (!resourceOwner.getKeycloakId().equals(keycloakId)) {
            throw new AccessDeniedException("You do not have access to this resource");
        }
    }
}
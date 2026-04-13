package com.danielerikssoncoder.cinema_project.repository;

import com.danielerikssoncoder.cinema_project.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Data access for customers.
 * <p>
 * findWithBookingsById and findWithTicketsById are kept as SEPARATE methods.
 * <p>
 * Loading both in one query would trigger Hibernate's MultipleBagFetchException
 * since two List relations cannot be JOIN FETCHed at the same time.
 * <p>
 * findMaxKeycloakNumber returns the highest sequence number among cinema-c{n}
 * accounts so CustomerService can generate the next available username.
 * <p>
 * COALESCE returns 0 when no such accounts exist yet.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Checks whether a username is already taken. */
    Optional<Customer> findByUsername(String username);

    /** Loads the customer with bookings, used for deletion check. */
    @EntityGraph(attributePaths = {"bookings"})
    Optional<Customer> findWithBookingsById(Long id);

    /** Loads the customer with tickets, used for deletion check. */
    @EntityGraph(attributePaths = {"tickets"})
    Optional<Customer> findWithTicketsById(Long id);

    /** Looks up a customer by their Keycloak UUID (sub claim in the JWT). */
    Optional<Customer> findByKeycloakId(String keycloakId);

    /**
     * Returns the highest sequence number among cinema-c{n} Keycloak usernames.
     * <p>
     * SUBSTRING(keycloakUsername, 9) strips "cinema-c" (8 chars) leaving the number.
     * <p>
     * Returns 0 if no such accounts exist.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.keycloakUsername, 9) AS int)), 0) FROM Customer c WHERE c.keycloakUsername LIKE 'cinema-c%'")
    Integer findMaxKeycloakNumber();
}
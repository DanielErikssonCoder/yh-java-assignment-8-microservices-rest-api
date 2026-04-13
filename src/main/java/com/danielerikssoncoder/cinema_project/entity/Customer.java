package com.danielerikssoncoder.cinema_project.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer in the system.
 * <p>
 * Each customer has a linked Keycloak account connected via:
 * <p>
 * keycloakId (Keycloak's internal UUID, used for authentication) and
 * keycloakUsername (for example: "cinema-c1", stored to calculate the next number).
 * <p>
 * Addresses are cascade-deleted with the customer.
 * <p>
 * Bookings and tickets are NOT cascaded and will block deletion if they exist.
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String keycloakUsername;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    // Stored locally to avoid querying Keycloak on every customer listing
    @Column(unique = true)
    private String email;

    // Keycloak's internal UUID, links the customer row to the Keycloak account
    @Column(unique = true)
    private String keycloakId;

    // Addresses are deleted automatically when the customer is deleted
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    // No cascade: these must be checked manually and will block deletion
    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "customer")
    private List<Ticket> tickets = new ArrayList<>();

    public Customer() {}

    /**
     * Convenience constructor used in CustomerService and DataSeeder.
     *
     * @param username   Unique login name
     * @param firstName  First name
     * @param lastName   Last name
     */
    public Customer(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }

    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }

    public String getKeycloakUsername() { return keycloakUsername; }
    public void setKeycloakUsername(String keycloakUsername) { this.keycloakUsername = keycloakUsername; }
}
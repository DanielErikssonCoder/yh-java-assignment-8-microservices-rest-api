package com.danielerikssoncoder.cinema_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Represents a postal address belonging to a customer.
 * <p>
 * A customer can have many addresses, but each address belongs to exactly one customer.
 * @JsonIgnore on the customer field prevents circular JSON serialization.
 */
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String country;

    /**
     * Reference back to the owning customer.
     * <p>
     * FetchType.LAZY means the customer is not loaded until explicitly accessed,
     * which avoids unnecessary JOIN queries when we only need the address itself.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    /** Required no-arg constructor for JPA. */
    public Address() {}

    /**
     * Convenience constructor used in service classes and DataSeeder.
     * The customer reference is set separately via setCustomer().
     *
     * @param street Street address
     * @param city City
     * @param zipCode Zip code
     * @param country Country
     */
    public Address(String street, String city, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public  String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}

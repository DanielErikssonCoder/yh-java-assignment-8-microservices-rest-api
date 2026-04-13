package com.danielerikssoncoder.cinema_project.service;

import com.danielerikssoncoder.cinema_project.dto.request.AddressRequest;
import com.danielerikssoncoder.cinema_project.dto.request.CustomerRequest;
import com.danielerikssoncoder.cinema_project.dto.response.CustomerResponse;
import com.danielerikssoncoder.cinema_project.entity.Address;
import com.danielerikssoncoder.cinema_project.entity.Customer;
import com.danielerikssoncoder.cinema_project.exception.CustomerHasActiveBookingsException;
import com.danielerikssoncoder.cinema_project.exception.CustomerHasActiveTicketsException;
import com.danielerikssoncoder.cinema_project.exception.ResourceNotFoundException;
import com.danielerikssoncoder.cinema_project.repository.AddressRepository;
import com.danielerikssoncoder.cinema_project.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for customer management. ADMIN only.
 * <p>
 * Handles creating and deleting customers including their Keycloak accounts.
 * <p>
 * Addresses are also managed here as a sub-resource of customers.
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final KeycloakService keycloakService;

    public CustomerService(CustomerRepository customerRepository,
                           AddressRepository addressRepository,
                           KeycloakService keycloakService) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.keycloakService = keycloakService;
    }

    /**
     * Returns all customers as DTOs.
     *
     * @return  List of all customers
     */
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    /**
     * Returns a specific customer by ID.
     * <p>
     * Throws 404 if not found.
     *
     * @param id Customer's database ID
     * @return The customer as a DTO
     */
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
        return CustomerResponse.fromEntity(customer);
    }

    /**
     * Creates a new customer and a Keycloak account.
     * <p>
     * The username duplicate check happens BEFORE the Keycloak call to prevent
     * orphan Keycloak accounts: if MySQL later threw a unique constraint error,
     * the Keycloak account would already exist with no linked customer row.
     * <p>
     * The Keycloak username is auto-generated as cinema-c{n}.
     *
     * @param request  Customer data including email and password for Keycloak
     * @return         The saved customer entity
     */
    public Customer createCustomer(CustomerRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required when creating a customer");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating a customer");
        }

        // Check for duplicate before calling Keycloak to avoid orphan accounts
        if (customerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("A record with that value already exists");
        }

        int nextNumber = customerRepository.findMaxKeycloakNumber() + 1;
        String keycloakUsername = "cinema-c" + nextNumber;

        String keycloakId = keycloakService.createUser(
                keycloakUsername,
                request.getPassword(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName()
        );

        Customer customer = new Customer(request.getUsername(), request.getFirstName(), request.getLastName());
        customer.setKeycloakId(keycloakId);
        customer.setKeycloakUsername(keycloakUsername);
        Customer saved = customerRepository.save(customer);
        logger.info("admin created customer '{} {}'", saved.getFirstName(), saved.getLastName());
        return saved;
    }

    /**
     * Updates a customer using PUT semantics (all fields replaced).
     * <p>
     * Validates that the new username does not belong to a different customer
     * to give a clear 409 response instead of a generic MySQL error.
     *
     * @param id Customer's database ID
     * @param request New customer data
     * @return The updated customer as a DTO
     */
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));

        // Allow keeping the same username, but not taking one from another customer
        customerRepository.findByUsername(request.getUsername())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DataIntegrityViolationException("A record with that value already exists");
                });

        customer.setUsername(request.getUsername());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        Customer updated = customerRepository.save(customer);
        logger.info("admin updated customer id {}", id);
        return CustomerResponse.fromEntity(updated);
    }

    /**
     * Deletes a customer and their Keycloak account.
     * <p>
     * Bookings and tickets are loaded in SEPARATE queries to avoid
     * Hibernate's MultipleBagFetchException when two List relations
     * are JOIN FETCHed at the same time.
     * <p>
     * Throws 409 if the customer has bookings or tickets.
     *
     * @param id  Customer's database ID
     */
    public void deleteCustomer(Long id) {

        // Load bookings first in a separate query
        Customer customerWithBookings = customerRepository.findWithBookingsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));

        if (!customerWithBookings.getBookings().isEmpty()) {
            throw new CustomerHasActiveBookingsException("Cannot delete customer with existing bookings.");
        }

        // Load tickets in a separate query to avoid MultipleBagFetchException
        Customer customerWithTickets = customerRepository.findWithTicketsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));

        if (!customerWithTickets.getTickets().isEmpty()) {
            throw new CustomerHasActiveTicketsException("Cannot delete customer with existing tickets.");
        }

        keycloakService.deleteUser(customerWithBookings.getKeycloakId());
        customerRepository.deleteById(id);
        logger.info("admin deleted customer id {}", id);
    }

    /**
     * Adds a new address to an existing customer.
     *
     * @param customerId Customer's database ID
     * @param request Address data
     * @return The saved address entity
     */
    public Address addAddress(Long customerId, AddressRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        Address address = new Address(request.getStreet(), request.getCity(), request.getZipCode(), request.getCountry());
        address.setCustomer(customer);
        Address saved = addressRepository.save(address);
        logger.info("admin added address to customer id {}", customerId);
        return saved;
    }

    /**
     * Removes an address from a customer.
     * <p>
     * Validates that the address actually belongs to the given customer,
     * otherwise returns 404 instead of deleting the wrong address.
     *
     * @param customerId Customer's database ID
     * @param addressId Address's database ID
     */
    public void deleteAddress(Long customerId, Long addressId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Make sure the address actually belongs to this customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Address does not belong to customer id: " + customerId);
        }

        customer.getAddresses().remove(address);
        addressRepository.delete(address);
        logger.info("admin deleted address id {} from customer id {}", addressId, customerId);
    }
}
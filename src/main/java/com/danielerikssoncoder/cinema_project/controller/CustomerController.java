package com.danielerikssoncoder.cinema_project.controller;

import com.danielerikssoncoder.cinema_project.dto.request.AddressRequest;
import com.danielerikssoncoder.cinema_project.dto.request.CustomerRequest;
import com.danielerikssoncoder.cinema_project.dto.response.AddressResponse;
import com.danielerikssoncoder.cinema_project.dto.response.CustomerResponse;
import com.danielerikssoncoder.cinema_project.entity.Address;
import com.danielerikssoncoder.cinema_project.entity.Customer;
import com.danielerikssoncoder.cinema_project.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Handles customer management endpoints.
 * <p>
 * All endpoints require ADMIN (set via /api/v1/customers/** in SecurityConfig).
 * Addresses are a sub-resource of customers and are handled here too.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * GET /api/v1/customers
     * <p>
     * Returns all customers.
     *
     * @return  List of all customers (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * GET /api/v1/customers/{customerId}
     * <p>
     * Returns a specific customer by ID.
     * Returns 404 if not found.
     *
     * @param customerId Database ID of the customer
     * @return The customer (200 OK) or 404
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    /**
     * POST /api/v1/customers
     * <p>
     * Creates a new customer and a Keycloak account.
     * <p>
     * Returns 201 Created with a Location header.
     * <p>
     * Returns 409 Conflict if the username is already taken.
     *
     * @param request Customer data (username, firstName, lastName, email, password)
     * @return The created customer (201 Created)
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer created = customerService.createCustomer(request);
        CustomerResponse response = CustomerResponse.fromEntity(created);
        URI location = URI.create("/api/v1/customers/" + created.getId());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * PUT /api/v1/customers/{customerId}
     * <p>
     * Updates a customer. PUT semantics: all fields must be sent.
     * <p>
     * Returns 404 if not found.
     *
     * @param customerId  Database ID of the customer
     * @param request     New customer data (all fields required)
     * @return            The updated customer (200 OK)
     */
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long customerId,
                                                           @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    /**
     * DELETE /api/v1/customers/{customerId}
     * <p>
     * Deletes a customer and their Keycloak account.
     * <p>
     * Returns 409 Conflict if the customer has bookings or tickets.
     * <p>
     * Returns 204 No Content on success.
     *
     * @param customerId Database ID of the customer
     * @return 204 No Content
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/customers/{customerId}/addresses
     * <p>
     * Adds a new address to an existing customer.
     * <p>
     * Returns 201 Created with a Location header.
     *
     * @param customerId  Database ID of the customer
     * @param request     Address data (street, city, zipCode, country)
     * @return            The created address (201 Created)
     */
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable Long customerId,
                                                      @Valid @RequestBody AddressRequest request) {
        Address created = customerService.addAddress(customerId, request);
        AddressResponse response = AddressResponse.fromEntity(created);
        URI location = URI.create("/api/v1/customers/" + customerId + "/addresses/" + created.getId());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * DELETE /api/v1/customers/{customerId}/addresses/{addressId}
     * <p>
     * Removes an address from a customer.
     * <p>
     * Returns 404 if the address does not belong to the given customer.
     *
     * @param customerId  Database ID of the customer
     * @param addressId   Database ID of the address
     * @return            204 No Content
     */
    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long customerId, @PathVariable Long addressId) {
        customerService.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}

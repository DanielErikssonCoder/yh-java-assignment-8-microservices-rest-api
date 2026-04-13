package com.danielerikssoncoder.cinema_project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Input for POST and PUT /api/v1/customers.
 * <p>
 * The same DTO is used for both, so PUT requires all fields (PUT semantics).
 * <p>
 * email and password are used during POST to create the Keycloak account.
 * <p>
 * On PUT, password is ignored since we do not update passwords via this API.
 */
public class CustomerRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email must be valid")
    private String email;

    // Required on POST, ignored on PUT
    private String password;

    public CustomerRequest() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
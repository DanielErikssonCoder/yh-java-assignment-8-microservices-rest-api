package com.danielerikssoncoder.cinema_project.dto.response;

import com.danielerikssoncoder.cinema_project.entity.Customer;

import java.util.List;

/**
 * Response representation of a customer.
 * <p>
 * keycloakUsername (for example: cinema-c1) is included so admins can see which
 * Keycloak account is linked. keycloakId (UUID) is excluded as it is internal.
 */
public class CustomerResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String keycloakUsername;
    private List<AddressResponse> addresses;

    public CustomerResponse() {}

    /**
     * Converts a Customer entity to a CustomerResponse DTO.
     * <p>
     * Maps each address via AddressResponse.fromEntity().
     *
     * @param customer Entity to convert
     * @return DTO ready for JSON serialization
     */
    public static CustomerResponse fromEntity(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setUsername(customer.getUsername());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setKeycloakUsername(customer.getKeycloakUsername());
        response.setAddresses(
                customer.getAddresses().stream()
                        .map(AddressResponse::fromEntity)
                        .toList()
        );
        return response;
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

    public String getKeycloakUsername() { return keycloakUsername; }
    public void setKeycloakUsername(String keycloakUsername) { this.keycloakUsername = keycloakUsername; }

    public List<AddressResponse> getAddresses() { return addresses; }
    public void setAddresses(List<AddressResponse> addresses) { this.addresses = addresses; }
}
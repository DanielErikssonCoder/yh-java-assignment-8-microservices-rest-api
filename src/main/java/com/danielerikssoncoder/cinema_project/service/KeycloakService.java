package com.danielerikssoncoder.cinema_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Communicates with Keycloak's Admin REST API to create and delete accounts.
 * <p>
 * All credentials and URLs come from application.yaml via @Value.
 * <p>
 * Jackson ObjectMapper is used for JSON building to prevent injection attacks.
 * <p>
 * Flow for creating a user:
 * <p>
 * 1. Get an admin token from the master realm.
 * <p>
 * 2. Create the user in the grupp-e realm.
 * <p>
 * 3. Fetch the user's UUID (keycloakId).
 * <p>
 * 4. Assign the USER role.
 * <p>
 * 5. Return keycloakId to link with the Customer row.
 */
@Service
public class KeycloakService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${keycloak.base-url}")
    private String keycloakBase;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    /**
     * Creates a Keycloak user, assigns the USER role, and returns the user's UUID.
     *
     * @param username Keycloak username, e.g. cinema-c6
     * @param password Password
     * @param email Email address
     * @param firstName First name
     * @param lastName Last name
     * @return Keycloak's internal UUID for the user
     */
    public String createUser(String username, String password, String email,
                             String firstName, String lastName) {
        String adminToken = getAdminToken();
        createKeycloakUser(adminToken, username, password, email, firstName, lastName);
        String keycloakId = getKeycloakUserId(adminToken, username);
        assignUserRole(adminToken, keycloakId);
        return keycloakId;
    }

    /**
     * Deletes a Keycloak user by their UUID.
     * <p>
     * Throws RuntimeException on failure so CustomerService rolls back the
     * transaction and we avoid leaving an orphan Keycloak account.
     * <p>
     * A null keycloakId is logged as a warning and skipped.
     *
     * @param keycloakId Keycloak's internal UUID for the user
     */
    public void deleteUser(String keycloakId) {
        if (keycloakId == null) {
            logger.warn("deleteUser called with null keycloakId — skipping");
            return;
        }

        String adminToken = getAdminToken();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakBase + "/admin/realms/" + realm + "/users/" + keycloakId))
                .header("Authorization", "Bearer " + adminToken)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 204) {
                logger.info("Keycloak user deleted: {}", keycloakId);
            } else {
                throw new RuntimeException("Failed to delete Keycloak user " + keycloakId
                        + ": " + response.statusCode() + " " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting Keycloak user " + keycloakId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Sends a POST request to create a new Keycloak user.
     * <p>
     * JSON is built with ObjectMapper instead of String.format() to prevent
     * injection if fields contain quotes or backslashes.
     * <p>
     * emailVerified must be true, otherwise Keycloak returns "Account is not fully set up".
     */
    private void createKeycloakUser(String adminToken, String username, String password,
                                    String email, String firstName, String lastName) {
        try {
            ObjectNode credential = objectMapper.createObjectNode();
            credential.put("type", "password");
            credential.put("value", password);
            credential.put("temporary", false);

            ArrayNode credentials = objectMapper.createArrayNode();
            credentials.add(credential);

            ObjectNode userJson = objectMapper.createObjectNode();
            userJson.put("username", username);
            userJson.put("email", email);
            userJson.put("firstName", firstName);
            userJson.put("lastName", lastName);
            userJson.put("enabled", true);
            userJson.put("emailVerified", true);
            userJson.set("credentials", credentials);

            String body = objectMapper.writeValueAsString(userJson);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(keycloakBase + "/admin/realms/" + realm + "/users"))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                logger.info("Keycloak user created: {}", username);
            } else if (response.statusCode() == 409) {
                // User already exists, log a warning but do not crash
                logger.warn("Keycloak user already exists: {}", username);
            } else {
                throw new RuntimeException("Failed to create Keycloak user " + username
                        + ": " + response.statusCode() + " " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Keycloak user " + username + ": " + e.getMessage(), e);
        }
    }

    /**
     * Assigns the USER role to a Keycloak user.
     * <p>
     * First fetches the role's representation (id + name), then POSTs it.
     *
     * @param adminToken Admin token
     * @param keycloakUserId Keycloak UUID of the user
     */
    private void assignUserRole(String adminToken, String keycloakUserId) {
        String roleJson = getRoleRepresentation(adminToken);
        if (roleJson == null) {
            throw new RuntimeException("Could not fetch USER role from Keycloak");
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(keycloakBase + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mappings/realm"))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("[" + roleJson + "]"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 204) {
                logger.info("Assigned USER role to keycloakId: {}", keycloakUserId);
            } else {
                throw new RuntimeException("Failed to assign USER role: " + response.statusCode() + " " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error assigning USER role: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches the JSON representation of the USER role from Keycloak.
     * <p>
     * Needed to assign the role to a user.
     *
     * @param adminToken Admin token
     * @return Role JSON string, or null if the request failed
     */
    private String getRoleRepresentation(String adminToken) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(keycloakBase + "/admin/realms/" + realm + "/roles/USER"))
                    .header("Authorization", "Bearer " + adminToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            logger.error("Error fetching USER role: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Fetches an admin token from Keycloak's master realm.
     * <p>
     * Credentials come from application.yaml. The token is parsed with Jackson.
     *
     * @return  Admin bearer token
     */
    private String getAdminToken() {
        try {
            String body = "grant_type=password"
                    + "&client_id=admin-cli"
                    + "&username=" + URLEncoder.encode(adminUsername, StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(adminPassword, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(keycloakBase + "/realms/master/protocol/openid-connect/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            return json.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Keycloak admin token", e);
        }
    }

    /**
     * Fetches Keycloak's internal UUID for a user by username.
     * <p>
     * Uses exact=true to prevent partial matches (for example: cinema-c1 matching cinema-c10).
     * <p>
     * The UUID is parsed with Jackson instead of string manipulation.
     *
     * @param adminToken  Admin token
     * @param username    Keycloak username to search for
     * @return            Keycloak's internal UUID
     */
    private String getKeycloakUserId(String adminToken, String username) {
        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(keycloakBase + "/admin/realms/" + realm + "/users?username=" + encodedUsername + "&exact=true"))
                    .header("Authorization", "Bearer " + adminToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());

            if (!json.isArray() || json.isEmpty()) {
                throw new RuntimeException("No Keycloak user found with username: " + username);
            }

            return json.get(0).get("id").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Keycloak user id for: " + username, e);
        }
    }
}
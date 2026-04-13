package com.danielerikssoncoder.cinema_project.controller;

import com.danielerikssoncoder.cinema_project.dto.request.TicketRequest;
import com.danielerikssoncoder.cinema_project.dto.response.TicketResponse;
import com.danielerikssoncoder.cinema_project.entity.Customer;
import com.danielerikssoncoder.cinema_project.entity.Ticket;
import com.danielerikssoncoder.cinema_project.service.AuthService;
import com.danielerikssoncoder.cinema_project.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;


import java.net.URI;
import java.util.List;

/**
 * Handles ticket endpoints.
 * <p>
 * Available to USER and ADMIN (permissions set in SecurityConfig).
 * <p>
 * The customerId is always taken from the JWT, never from the client.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final AuthService authService;

    public TicketController(TicketService ticketService, AuthService authService) {
        this.ticketService = ticketService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/tickets
     * <p>
     * Returns all tickets for the logged-in customer.
     * <p>
     * If customerId is provided, we verify it matches the logged-in customer,
     * otherwise we throw 403.
     *
     * @param customerId Optional query param required by the spec
     * @param token JWT token from the current user
     * @return List of the customer's tickets (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getTickets(
            @RequestParam(required = false) Long customerId,
            JwtAuthenticationToken token) {
        Customer current = authService.getCurrentCustomer(token);
        if (customerId != null && !current.getId().equals(customerId)) {
            throw new AccessDeniedException("You do not have access to this resource");
        }
        Long id = (customerId != null) ? customerId : current.getId();
        return ResponseEntity.ok(ticketService.getTicketsByCustomerId(id));
    }

    /**
     * POST /api/v1/tickets
     * <p>
     * Purchases tickets for a screening.
     * <p>
     * The customerId is set from the JWT, not sent in the request body.
     * <p>
     * Returns 400 if not enough seats are available.
     * <p>
     * Returns 404 if the screening does not exist.
     * <p>
     * Returns 201 Created with a Location header on success.
     *
     * @param request Ticket data (screeningId, numberOfTickets)
     * @param token JWT token to identify the customer
     * @return The purchased ticket record (201 Created)
     */
    @PostMapping
    public ResponseEntity<TicketResponse> purchaseTicket(
            @Valid @RequestBody TicketRequest request,
            JwtAuthenticationToken token) {
        Customer current = authService.getCurrentCustomer(token);
        request.setCustomerId(current.getId());
        Ticket created = ticketService.purchaseTicket(request);
        TicketResponse response = TicketResponse.fromEntity(created);
        URI location = URI.create("/api/v1/tickets/" + created.getId());
        return ResponseEntity.created(location).body(response);
    }
}
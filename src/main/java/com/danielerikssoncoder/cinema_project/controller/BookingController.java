package com.danielerikssoncoder.cinema_project.controller;

import com.danielerikssoncoder.cinema_project.dto.request.BookingRequest;
import com.danielerikssoncoder.cinema_project.dto.request.BookingUpdateRequest;
import com.danielerikssoncoder.cinema_project.dto.response.BookingResponse;
import com.danielerikssoncoder.cinema_project.entity.Booking;
import com.danielerikssoncoder.cinema_project.entity.Customer;
import com.danielerikssoncoder.cinema_project.service.AuthService;
import com.danielerikssoncoder.cinema_project.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

import java.net.URI;
import java.util.List;

/**
 * Handles booking endpoints.
 * <p>
 * A booking reserves an entire room for a private event, as opposed to
 * tickets which are purchased for a specific screening.
 * Available to both USER and ADMIN (permissions set in SecurityConfig).
 * The customerId is always taken from the JWT, never from the client.
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    public BookingController(BookingService bookingService, AuthService authService) {
        this.bookingService = bookingService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/bookings
     * <p>
     * Returns bookings for the logged-in customer.
     * <p>
     * If customerId is provided as a query param, we verify it matches
     * the logged-in customer, otherwise we throw 403.
     *
     * @param customerId  Optional query param required by the spec
     * @param token       JWT token from the current user
     * @return            List of the customer's bookings
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getBookings(
            @RequestParam(required = false) Long customerId,
            JwtAuthenticationToken token) {
        Customer current = authService.getCurrentCustomer(token);
        if (customerId != null && !current.getId().equals(customerId)) {
            throw new AccessDeniedException("You do not have access to this resource");
        }
        Long id = (customerId != null) ? customerId : current.getId();
        return ResponseEntity.ok(bookingService.getBookingsByCustomerId(id));
    }

    /**
     * POST /api/v1/bookings
     * <p>
     * Creates a new booking for the logged-in customer.
     * <p>
     * The customerId is set from the JWT, not sent by the client.
     * Returns 201 Created with a Location header.
     *
     * @param request  Booking data (roomId, date, numberOfGuests, etc.)
     * @param token    JWT token to identify the customer
     * @return         The created booking with 201 Created
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            JwtAuthenticationToken token) {
        Customer current = authService.getCurrentCustomer(token);
        request.setCustomerId(current.getId());
        Booking created = bookingService.createBooking(request);
        BookingResponse response = BookingResponse.fromEntity(created);
        URI location = URI.create("/api/v1/bookings/" + created.getId());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * PATCH /api/v1/bookings/{bookingId}
     * <p>
     * Updates the date and/or technical equipment of a booking.
     * <p>
     * The booking is fetched once here and passed to updateBooking()
     * to avoid an extra database call inside the service layer.
     * verifyOwnership() ensures only the booking's owner can update it.
     *
     * @param bookingId  ID of the booking to update
     * @param request    Fields to update (all optional, empty body is fine)
     * @param token      JWT token for ownership verification
     * @return           The updated booking
     */
    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long bookingId,
            @RequestBody BookingUpdateRequest request,
            JwtAuthenticationToken token) {
        Booking booking = bookingService.getBookingEntity(bookingId);
        authService.verifyOwnership(booking.getCustomer(), token);
        return ResponseEntity.ok(bookingService.updateBooking(booking, request));
    }
}
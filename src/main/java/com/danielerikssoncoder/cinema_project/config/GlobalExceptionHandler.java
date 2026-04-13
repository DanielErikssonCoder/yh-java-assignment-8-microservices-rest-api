package com.danielerikssoncoder.cinema_project.config;

import com.danielerikssoncoder.cinema_project.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all exceptions thrown in controllers and services
 * and returns a nice JSON response with the correct HTTP status code.
 * <p>
 * Without this class, Spring would have returned its own error formatting
 * which is hard to read. Now we always get the same structure:
 * { "timestamp", "status", "error", "message" }
 *
 * @RestControllerAdvice means that this class applies to all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 404 Not Found. Thrown when an ID lookup finds nothing in the database.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 400 Bad Request. Thrown in BookingService when numberOfGuests exceeds room capacity.
     */
    @ExceptionHandler(RoomCapacityExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRoomCapacity(RoomCapacityExceededException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 400 Bad Request. Thrown in TicketService when the customer tries to buy more tickets
     * than are available for the screening.
     */
    @ExceptionHandler(NotEnoughSeatsException.class)
    public ResponseEntity<Map<String, Object>> handleNotEnoughSeats(NotEnoughSeatsException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 400 Bad Request. Thrown in CustomerService when email or password is missing on POST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 409 Conflict. Thrown when an admin tries to delete a customer who has bookings.
     */
    @ExceptionHandler(CustomerHasActiveBookingsException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerHasBookings(CustomerHasActiveBookingsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 409 Conflict. Thrown when an admin tries to delete a customer who has tickets.
     */
    @ExceptionHandler(CustomerHasActiveTicketsException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerHasTickets(CustomerHasActiveTicketsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 409 Conflict. Thrown when an admin tries to delete a movie that has screenings.
     */
    @ExceptionHandler(MovieHasScreeningsException.class)
    public ResponseEntity<Map<String, Object>> handleMovieHasScreenings(MovieHasScreeningsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 409 Conflict. Thrown when an admin tries to delete a room that has screenings.
     */
    @ExceptionHandler(RoomHasScreeningsException.class)
    public ResponseEntity<Map<String, Object>> handleRoomHasScreenings(RoomHasScreeningsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 409 Conflict. Thrown when an admin tries to delete a room that has bookings.
     */
    @ExceptionHandler(RoomHasBookingsException.class)
    public ResponseEntity<Map<String, Object>> handleRoomHasBookings(RoomHasBookingsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 409 Conflict. Thrown when an admin tries to delete a screening that has sold tickets.
     */
    @ExceptionHandler(ScreeningHasTicketsException.class)
    public ResponseEntity<Map<String, Object>> handleScreeningHasTickets(ScreeningHasTicketsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * 409 Conflict. Catches MySQL unique constraint violations, like duplicate username.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateKey(DataIntegrityViolationException ex) {
        return buildResponse(HttpStatus.CONFLICT, "A record with that value already exists");
    }

    /**
     * 400 Bad Request.Thrown automatically by Spring when @Valid validation fails.
     * <p>
     * The response includes a map of all fields that failed validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 403 Forbidden. Thrown by AuthService.verifyOwnership() or directly in controllers
     * when a customer tries to access a resource that belongs to someone else.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have access to this resource");
    }

    /**
     * 500 Internal Server Error. Catches any exception that has no specific handler.
     * <p>
     * The stack trace is logged so we can debug in wigell-cinema.log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    /**
     * Builds a consistent JSON error body with four fields:
     * { "timestamp", "status", "error", "message" }
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
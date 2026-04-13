package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown when an admin tries to delete a customer who still has bookings.
 * <p>
 * Mapped to HTTP 409 Conflict in GlobalExceptionHandler.
 * <p>
 * We use separate exception classes per business constraint so the class
 * name is self-documenting and each case can be handled individually.
 * <p>
 * Extends RuntimeException so callers do not need to declare it.
 */
public class CustomerHasActiveBookingsException extends RuntimeException {

    public CustomerHasActiveBookingsException(String message) {
        super(message);
    }
}
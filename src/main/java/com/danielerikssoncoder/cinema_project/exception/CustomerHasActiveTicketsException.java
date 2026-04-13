package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown when an admin tries to delete a customer who has purchased tickets.
 * <p>
 * Mapped to HTTP 409 Conflict in GlobalExceptionHandler.
 */
public class CustomerHasActiveTicketsException extends RuntimeException {

    public CustomerHasActiveTicketsException(String message) {
        super(message);
    }
}
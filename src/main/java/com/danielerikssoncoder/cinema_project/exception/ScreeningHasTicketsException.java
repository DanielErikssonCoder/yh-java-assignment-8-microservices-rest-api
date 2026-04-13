package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown when an admin tries to delete a screening that has sold tickets.
 * <p>
 * Mapped to HTTP 409 Conflict in GlobalExceptionHandler.
 * <p>
 * Deleting a screening with sold tickets would leave orphaned Ticket rows
 * and destroy the customer's purchase history. This check prevents that.
 */
public class ScreeningHasTicketsException extends RuntimeException {

    public ScreeningHasTicketsException(String message) {
        super(message);
    }
}
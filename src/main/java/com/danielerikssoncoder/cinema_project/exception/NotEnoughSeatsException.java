package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown in TicketService when the customer tries to buy more tickets than are available on the screening.
 * <p>
 * Mapped to HTTP 400 Bad Request in GlobalExceptionHandler.
 * <p>
 * We use 400 instead of 409 because the client's input is invalid
 * relative to the current state, not a conflict between two resources.
 */
public class NotEnoughSeatsException extends RuntimeException {

    public NotEnoughSeatsException(String message) {
        super(message);
    }
}
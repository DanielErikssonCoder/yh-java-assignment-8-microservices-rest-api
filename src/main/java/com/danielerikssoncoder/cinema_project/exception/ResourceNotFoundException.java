package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown when an ID lookup finds nothing in the database.
 * <p>
 * Mapped to HTTP 404 Not Found in GlobalExceptionHandler.
 * <p>
 * Used for all entity types: Customer, Movie, Room, Screening, Booking, Ticket, Address.
 * <p>
 * One general exception keeps the number of classes and handlers low.
 * <p>
 * The message string says which type was missing.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
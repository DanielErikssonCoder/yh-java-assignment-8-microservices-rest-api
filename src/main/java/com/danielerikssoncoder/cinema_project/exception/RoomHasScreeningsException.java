package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown when an admin tries to delete a room that has linked screenings.
 * <p>
 * Mapped to HTTP 409 Conflict in GlobalExceptionHandler.
 * <p>
 * Correct deletion order: tickets then screenings then room.
 */
public class RoomHasScreeningsException extends RuntimeException {

    public RoomHasScreeningsException(String message) {
        super(message);
    }
}
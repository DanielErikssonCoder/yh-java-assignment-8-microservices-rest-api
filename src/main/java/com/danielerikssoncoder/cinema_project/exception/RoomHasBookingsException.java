package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown when an admin tries to delete a room that has active bookings.
 * <p>
 * Mapped to HTTP 409 Conflict in GlobalExceptionHandler.
 * <p>
 * A room can have two types of dependencies: screenings and direct bookings.
 * <p>
 * RoomService checks both with separate exceptions for specific error messages.
 */
public class RoomHasBookingsException extends RuntimeException {

    public RoomHasBookingsException(String message) {
        super(message);
    }
}
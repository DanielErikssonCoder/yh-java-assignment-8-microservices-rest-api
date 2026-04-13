package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown in BookingService when numberOfGuests exceeds the room's maxGuests.
 * <p>
 * Mapped to HTTP 400 Bad Request in GlobalExceptionHandler.
 * <p>
 * The message includes the room's actual capacity so the client knows
 * what value is valid, for example: "Number of guests exceeds room capacity of 50".
 */
public class RoomCapacityExceededException extends RuntimeException {

    public RoomCapacityExceededException(String message) {
        super(message);
    }
}
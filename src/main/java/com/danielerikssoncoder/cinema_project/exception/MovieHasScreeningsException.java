package com.danielerikssoncoder.cinema_project.exception;

/**
 * Thrown when an admin tries to delete a movie that has linked screenings.
 * <p>
 * Mapped to HTTP 409 Conflict in GlobalExceptionHandler.
 * <p>
 * Without this check, MySQL would throw a cryptic FK violation error.
 * <p>
 * This class produces a clear message like "Delete screenings first."
 */
public class MovieHasScreeningsException extends RuntimeException {

    public MovieHasScreeningsException(String message) {
        super(message);
    }
}
package com.danielerikssoncoder.cinema_project.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Input for POST /api/v1/screenings (ADMIN).
 * <p>
 * availableSeats is not sent here, it is set automatically in the Screening constructor.
 * @Positive is used instead of @Min(1) on pricePerTicket since the value is a double.
 */
public class ScreeningRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;

    @Positive(message = "Price per ticket must be positive")
    private double pricePerTicket;

    public ScreeningRequest() {}

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public double getPricePerTicket() {
        return pricePerTicket;
    }

    public void setPricePerTicket(double pricePerTicket) {
        this.pricePerTicket = pricePerTicket;
    }
}

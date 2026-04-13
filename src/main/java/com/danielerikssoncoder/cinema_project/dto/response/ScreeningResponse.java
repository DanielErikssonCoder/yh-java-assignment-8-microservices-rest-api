package com.danielerikssoncoder.cinema_project.dto.response;

import com.danielerikssoncoder.cinema_project.entity.Screening;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response representation of a screening.
 * <p>
 * Relations are flattened: movieId + movieTitle instead of a full MovieResponse,
 * and roomId + roomName instead of a full RoomResponse.
 * <p>
 * All ScreeningRepository list methods use @EntityGraph to ensure movie and room
 * are loaded before fromEntity() is called, preventing LazyInitializationException.
 */
public class ScreeningResponse {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long roomId;
    private String roomName;
    private LocalDate date;
    private LocalTime time;
    private double pricePerTicket;
    private int availableSeats;

    public ScreeningResponse() {}

    /**
     * Converts a Screening entity to a ScreeningResponse DTO.
     * <p>
     * Requires movie and room to be loaded (via @EntityGraph).
     *
     * @param screening Entity to convert
     * @return DTO ready for JSON serialization
     */
    public static ScreeningResponse fromEntity(Screening screening) {
        ScreeningResponse response = new ScreeningResponse();
        response.setId(screening.getId());
        response.setMovieId(screening.getMovie().getId());
        response.setMovieTitle(screening.getMovie().getTitle());
        response.setRoomId(screening.getRoom().getId());
        response.setRoomName(screening.getRoom().getName());
        response.setDate(screening.getDate());
        response.setTime(screening.getTime());
        response.setPricePerTicket(screening.getPricePerTicket());
        response.setAvailableSeats(screening.getAvailableSeats());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}

package com.danielerikssoncoder.cinema_project.dto.response;

import com.danielerikssoncoder.cinema_project.entity.Ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response representation of a ticket purchase.
 * <p>
 * This DTO has the deepest nesting in the project. The full object graph s flattened so the client sees
 * (example: "The Matrix, Salong 1, 18:00 on May 15") without extra API calls:
 * <p>
 * Ticket to Screening to Movie (movieTitle) and Room (roomName).
 * <p>
 * TicketRepository.findByCustomerId() uses @EntityGraph with
 * {"screening.movie", "screening.room", "customer"} to ensure the full
 * graph is loaded before fromEntity() runs.
 * <p>
 * screeningDate and screeningTime use the "screening" prefix to distinguish
 * them from the purchasedAt timestamp.
 */
public class TicketResponse {

    private Long id;
    private Long customerId;
    private Long screeningId;
    private String movieTitle;
    private String roomName;
    private LocalDate screeningDate;
    private LocalTime screeningTime;
    private int numberOfTickets;
    private double totalPriceSek;
    private double totalPriceUsd;
    private LocalDateTime purchasedAt;

    public TicketResponse() {}

    /**
     * Converts a Ticket entity to a TicketResponse DTO.
     * <p>
     * Requires screening, screening.movie and screening.room to be loaded.
     *
     * @param ticket  Entity to convert
     * @return        DTO ready for JSON serialization
     */
    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setCustomerId(ticket.getCustomer().getId());
        response.setScreeningId(ticket.getScreening().getId());
        response.setMovieTitle(ticket.getScreening().getMovie().getTitle());
        response.setRoomName(ticket.getScreening().getRoom().getName());
        response.setScreeningDate(ticket.getScreening().getDate());
        response.setScreeningTime(ticket.getScreening().getTime());
        response.setNumberOfTickets(ticket.getNumberOfTickets());
        response.setTotalPriceSek(ticket.getTotalPriceSek());
        response.setTotalPriceUsd(ticket.getTotalPriceUsd());
        response.setPurchasedAt(ticket.getPurchasedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Long screeningId) {
        this.screeningId = screeningId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LocalDate getScreeningDate() {
        return screeningDate;
    }

    public void setScreeningDate(LocalDate screeningDate) {
        this.screeningDate = screeningDate;
    }

    public LocalTime getScreeningTime() {
        return screeningTime;
    }

    public void setScreeningTime(LocalTime screeningTime) {
        this.screeningTime = screeningTime;
    }

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public double getTotalPriceSek() {
        return totalPriceSek;
    }

    public void setTotalPriceSek(double totalPriceSek) {
        this.totalPriceSek = totalPriceSek;
    }

    public double getTotalPriceUsd() {
        return totalPriceUsd;
    }

    public void setTotalPriceUsd(double totalPriceUsd) {
        this.totalPriceUsd = totalPriceUsd;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }
}

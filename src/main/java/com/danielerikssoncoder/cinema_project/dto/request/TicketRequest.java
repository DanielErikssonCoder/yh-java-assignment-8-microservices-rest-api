package com.danielerikssoncoder.cinema_project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Input for POST /api/v1/tickets.
 * <p>
 * customerId is set by the controller from the JWT, not sent by the client.
 * <p>
 * The upper limit on numberOfTickets is enforced by screening.availableSeats in TicketService.
 * <p>
 * Price is not sent here, it is calculated as pricePerTicket times numberOfTickets.
 */
public class TicketRequest {

    // Set from the JWT by the controller, not sent by the client
    private Long customerId;

    @NotNull(message = "Screening ID is required")
    private Long screeningId;

    @Min(value = 1, message = "Must buy at least 1 ticket")
    private int numberOfTickets;

    public TicketRequest() {
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

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }
}

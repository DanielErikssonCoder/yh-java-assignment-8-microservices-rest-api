package com.danielerikssoncoder.cinema_project.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Input for POST /api/v1/bookings.
 * <p>
 * customerId is set by the controller from the JWT, not sent by the client.
 * <p>
 * performance and technicalEquipment are optional (nullable).
 * <p>
 * The upper limit on numberOfGuests is validated against room capacity in BookingService.
 */
public class BookingRequest {

    // Set from the JWT by the controller, not sent by the client
    private Long customerId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    private LocalDate date;

    @Min(value = 1, message = "Number of guests must be at least 1")
    private int numberOfGuests;

    // Optional event description, for example: "Private screening: The Matrix"
    private String performance;

    // Optional requested equipment, for example: "Microphone, 4K Projector"
    private String technicalEquipment;

    public BookingRequest() {}

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    public String getTechnicalEquipment() {
        return technicalEquipment;
    }

    public void setTechnicalEquipment(String technicalEquipment) {
        this.technicalEquipment = technicalEquipment;
    }
}

package com.danielerikssoncoder.cinema_project.dto.request;

import java.time.LocalDate;

/**
 * Input for PATCH /api/v1/bookings/{bookingId}.
 * <p>
 * The spec only allows updating date and technical equipment.
 * <p>
 * All fields are optional: only send what should change.
 * <p>
 * An empty body is valid and results in no change.
 */
public class BookingUpdateRequest {

    // null means "do not change the date"
    private LocalDate date;

    // null means "do not change the equipment"
    private String technicalEquipment;

    public BookingUpdateRequest() {}

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTechnicalEquipment() {
        return technicalEquipment;
    }

    public void setTechnicalEquipment(String technicalEquipment) {
        this.technicalEquipment = technicalEquipment;
    }
}

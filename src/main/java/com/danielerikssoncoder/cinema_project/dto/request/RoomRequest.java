package com.danielerikssoncoder.cinema_project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Input for POST and PUT /api/v1/rooms.
 * <p>
 * @Min(1) on maxGuests: a room with no capacity would break the capacity
 * validation in BookingService. technicalEquipment is optional (nullable).
 */
public class RoomRequest {

    @NotBlank(message = "Room name is required")
    private String name;

    @Min(value = 1, message = "Max guests must be at least 1")
    private int maxGuests;

    // Optional: null means no equipment specified
    private String technicalEquipment;

    public RoomRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public String getTechnicalEquipment() {
        return technicalEquipment;
    }

    public void setTechnicalEquipment(String technicalEquipment) {
        this.technicalEquipment = technicalEquipment;
    }
}

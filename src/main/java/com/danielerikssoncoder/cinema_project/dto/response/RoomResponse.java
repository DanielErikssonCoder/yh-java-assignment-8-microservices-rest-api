package com.danielerikssoncoder.cinema_project.dto.response;

import com.danielerikssoncoder.cinema_project.entity.Room;

/**
 * Response representation of a room.
 * <p>
 * technicalEquipment can be null; Jackson serializes it as JSON null,
 * which the client treats as "no equipment specified".
 */
public class RoomResponse {

    private Long id;
    private String name;
    private int maxGuests;
    private String technicalEquipment;

    public RoomResponse() {}

    /**
     * Converts a Room entity to a RoomResponse DTO.
     *
     * @param room Entity to convert
     * @return DTO ready for JSON serialization
     */
    public static RoomResponse fromEntity(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setMaxGuests(room.getMaxGuests());
        response.setTechnicalEquipment(room.getTechnicalEquipment());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

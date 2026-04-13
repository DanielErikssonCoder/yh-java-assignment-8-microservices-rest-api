package com.danielerikssoncoder.cinema_project.entity;

import jakarta.persistence.*;

/**
 * Represents a cinema room that can be used in two ways:
 * as a venue for a Screening (movie shown to the public), or
 * as a location for a Booking (customer rents the whole room).
 * <p>
 * maxGuests is used for capacity validation in BookingService and
 * as the initial value for availableSeats in new Screenings.
 * <p>
 * technicalEquipment is nullable since not all rooms have special gear.
 */
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int maxGuests;

    private String technicalEquipment;

    public Room() {}

    /**
     * Convenience constructor used in DataSeeder and RoomService.
     *
     * @param name Room name
     * @param maxGuests Max number of guests
     * @param technicalEquipment Technical equipment (can be null)
     */
    public Room(String name, int maxGuests, String technicalEquipment) {
        this.name = name;
        this.maxGuests = maxGuests;
        this.technicalEquipment = technicalEquipment;
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

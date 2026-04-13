package com.danielerikssoncoder.cinema_project.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a room booking for a private event.
 * <p>
 * A Booking reserves an entire room ( private screening, company talk, etc. ).
 * <p>
 * A Ticket is for buying individual seats to a specific screening.
 * <p>
 * The price is stored in both SEK and USD to preserve historical rates and avoid reconverting on every read.
 * <p>
 * createdAt is set automatically in the constructor.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private int numberOfGuests;

    @Column(nullable = false)
    private LocalDate date;

    // Optional description, for example: "Private screening: The Matrix"
    private String performance;

    @Column(nullable = false)
    private double totalPriceSek;

    @Column(nullable = false)
    private double totalPriceUsd;

    // Optional requested equipment, for example: "Microphone, 4K Projector"
    private String technicalEquipment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * No-arg constructor required by JPA.
     * <p>
     * Also sets createdAt to now, avoiding the need for separate auditing annotations.
     */
    public Booking() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
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

    public String getTechnicalEquipment() {
        return technicalEquipment;
    }

    public void setTechnicalEquipment(String technicalEquipment) {
        this.technicalEquipment = technicalEquipment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}

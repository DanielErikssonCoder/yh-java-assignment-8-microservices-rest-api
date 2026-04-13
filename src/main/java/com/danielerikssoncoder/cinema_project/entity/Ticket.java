package com.danielerikssoncoder.cinema_project.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a ticket purchase for a screening.
 * <p>
 * One Ticket row can represent multiple tickets from the same purchase.
 * <p>
 * If a customer buys 3 tickets, one row is created with numberOfTickets=3.
 * <p>
 * The price is stored in both SEK and USD to preserve historical rates.
 * <p>
 * purchasedAt is set automatically in the constructor.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Holds the movie, room, date and time info for this ticket
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @Column(nullable = false)
    private int numberOfTickets;

    @Column(nullable = false)
    private double totalPriceSek;

    @Column(nullable = false)
    private double totalPriceUsd;

    // Purchase timestamp, set automatically in the constructor
    @Column(nullable = false)
    private LocalDateTime purchasedAt;


    // Sets purchasedAt to now, same pattern as Booking.createdAt.
    public Ticket() {
        this.purchasedAt = LocalDateTime.now();
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

    public Screening getScreening() {
        return screening;
    }

    public void setScreening(Screening screening) {
        this.screening = screening;
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

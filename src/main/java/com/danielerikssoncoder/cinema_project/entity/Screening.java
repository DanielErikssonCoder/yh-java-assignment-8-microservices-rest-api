package com.danielerikssoncoder.cinema_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a showing of a movie in a room at a specific date and time.
 * <p>
 * availableSeats starts at room.getMaxGuests() and decreases as tickets are purchased.
 */
@Entity
@Table(name = "screenings")
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * LAZY loading: the movie is not fetched until explicitly accessed.
     * @JsonIgnore prevents circular serialization back to Movie.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnore
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private double pricePerTicket;

    // Starts at room.getMaxGuests() and decreases as tickets are purchased
    private int availableSeats;

    public Screening() {}

    /**
     * Convenience constructor that sets availableSeats automatically.
     * <p>
     * A new screening always starts with all seats available.
     *
     * @param movie The movie being shown
     * @param room The room it is shown in
     * @param date Date of the screening
     * @param time Start time
     * @param pricePerTicket Price per ticket in SEK
     */
    public Screening(Movie movie, Room room, LocalDate date, LocalTime time, double pricePerTicket) {
        this.movie = movie;
        this.room = room;
        this.date = date;
        this.time = time;
        this.pricePerTicket = pricePerTicket;
        this.availableSeats = room.getMaxGuests();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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

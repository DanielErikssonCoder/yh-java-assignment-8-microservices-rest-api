package com.danielerikssoncoder.cinema_project.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a movie in the system.
 * <p>
 * A movie can have many screenings. MovieService checks for linked
 * screenings before allowing deletion to produce a clear 409 error
 * instead of a cryptic MySQL FK violation.
 */
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String genre;

    // Filmlängd i minuter — enklare att hantera som int än Duration
    @Column(nullable = false)
    private int lengthMinutes;

    // Åldersgräns i år, t.ex. 0 (barnfilm), 7, 11, 15
    @Column(nullable = false)
    private int ageLimit;

    // No cascade: screenings are checked manually in MovieService before deletion
    @OneToMany(mappedBy = "movie")
    private List<Screening> screenings = new ArrayList<>();

    public Movie() {}

    /**
     * Convenience constructor used in DataSeeder.
     *
     * @param title Movie title
     * @param genre Genre, for example: "Sci-Fi"
     * @param lengthMinutes Length in minutes
     * @param ageLimit Age limit in years
     */
    public Movie(String title, String genre, int lengthMinutes, int ageLimit) {
        this.title = title;
        this.genre = genre;
        this.lengthMinutes = lengthMinutes;
        this.ageLimit = ageLimit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getLengthMinutes() {
        return lengthMinutes;
    }

    public void setLengthMinutes(int lengthMinutes) {
        this.lengthMinutes = lengthMinutes;
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(int ageLimit) {
        this.ageLimit = ageLimit;
    }

    public List<Screening> getScreenings() {
        return screenings;
    }

    public void setScreenings(List<Screening> screenings) {
        this.screenings = screenings;
    }
}

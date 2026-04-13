package com.danielerikssoncoder.cinema_project.dto.response;

import com.danielerikssoncoder.cinema_project.entity.Movie;

/**
 * Response representation of a movie.
 * <p>
 * The screenings list is excluded intentionally; screenings are fetched
 * via GET /api/v1/screenings?movieId={id} when the client needs them.
 */
public class MovieResponse {

    private Long id;
    private String title;
    private String genre;
    private int lengthMinutes;
    private int ageLimit;

    public MovieResponse() {}

    /**
     * Converts a Movie entity to a MovieResponse DTO.
     *
     * @param movie  Entity to convert
     * @return       DTO ready for JSON serialization
     */
    public static MovieResponse fromEntity(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setGenre(movie.getGenre());
        response.setLengthMinutes(movie.getLengthMinutes());
        response.setAgeLimit(movie.getAgeLimit());
        return response;
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
}

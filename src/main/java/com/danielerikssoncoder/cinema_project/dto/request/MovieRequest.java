package com.danielerikssoncoder.cinema_project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Input for POST /api/v1/movies (ADMIN).
 * <p>
 * @Min(1) on lengthMinutes: a movie must be at least 1 minute long.
 * @Min(0) on ageLimit: 0 is valid (family film), negative is not.
 */
public class MovieRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Genre is required")
    private String genre;

    @Min(value = 1, message = "Length must be at least 1 minute")
    private int lengthMinutes;

    @Min(value = 0, message = "Age limit cannot be negative")
    private int ageLimit;

    public MovieRequest() {
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

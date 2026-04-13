package com.danielerikssoncoder.cinema_project.controller;

import com.danielerikssoncoder.cinema_project.dto.request.MovieRequest;
import com.danielerikssoncoder.cinema_project.dto.response.MovieResponse;
import com.danielerikssoncoder.cinema_project.entity.Movie;
import com.danielerikssoncoder.cinema_project.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Handles movie endpoints.
 * <p>
 * GET is available to USER and ADMIN. POST and DELETE require ADMIN.
 */
@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * GET /api/v1/movies
     * <p>
     * Returns all movies.
     *
     * @return  List of all movies (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    /**
     * GET /api/v1/movies/{movieId}
     * <p>
     * Returns a specific movie by ID.
     * <p>
     * Returns 404 if not found.
     *
     * @param movieId  Database ID of the movie
     * @return         The movie (200 OK) or 404
     */
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long movieId) {
        return ResponseEntity.ok(movieService.getMovieById(movieId));
    }

    /**
     * POST /api/v1/movies
     * <p>
     * Creates a new movie. Requires ADMIN.
     * <p>
     * Returns 201 Created with a Location header.
     *
     * @param request  Movie data (title, genre, lengthMinutes, ageLimit)
     * @return         The created movie (201 Created)
     */
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        Movie created = movieService.createMovie(request);
        MovieResponse response = MovieResponse.fromEntity(created);
        URI location = URI.create("/api/v1/movies/" + created.getId());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * DELETE /api/v1/movies/{movieId}
     * <p>
     * Deletes a movie. Requires ADMIN.
     * <p>
     * Returns 409 Conflict if the movie has screenings.
     * <p>
     * Returns 204 No Content on success.
     *
     * @param movieId  Database ID of the movie
     * @return         204 No Content
     */
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }
}

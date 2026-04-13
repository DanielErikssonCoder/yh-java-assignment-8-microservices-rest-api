package com.danielerikssoncoder.cinema_project.service;

import com.danielerikssoncoder.cinema_project.dto.request.MovieRequest;
import com.danielerikssoncoder.cinema_project.dto.response.MovieResponse;
import com.danielerikssoncoder.cinema_project.entity.Movie;
import com.danielerikssoncoder.cinema_project.exception.MovieHasScreeningsException;
import com.danielerikssoncoder.cinema_project.exception.ResourceNotFoundException;
import com.danielerikssoncoder.cinema_project.repository.MovieRepository;
import com.danielerikssoncoder.cinema_project.repository.ScreeningRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for movies.
 * <p>
 * GET is available to USER and ADMIN. POST and DELETE require ADMIN (set in SecurityConfig).
 * <p>
 * ScreeningRepository is injected directly here instead of using ScreeningService
 * to avoid a potential circular dependency (Movie to Screening to Movie).
 */
@Service
@Transactional
public class MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;

    public MovieService(MovieRepository movieRepository, ScreeningRepository screeningRepository) {
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
    }

    /**
     * Returns all movies as DTOs.
     *
     * @return  List of all movies
     */
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(MovieResponse::fromEntity)
                .toList();
    }

    /**
     * Returns a specific movie by ID.
     * <p>
     * Throws 404 if not found.
     *
     * @param id Movie's database ID
     * @return The movie as a DTO
     */
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return MovieResponse.fromEntity(movie);
    }

    /**
     * Creates a new movie.
     *
     * @param request Movie data (title, genre, lengthMinutes, ageLimit)
     * @return The saved movie entity
     */
    public Movie createMovie(MovieRequest request) {
        Movie movie = new Movie(request.getTitle(), request.getGenre(), request.getLengthMinutes(), request.getAgeLimit());
        Movie saved = movieRepository.save(movie);
        logger.info("admin created movie '{}'", saved.getTitle());
        return saved;
    }

    /**
     * Deletes a movie if no screenings are linked to it.
     * <p>
     * existsById() is checked first so we return a clear 404 when the movie
     * does not exist, not a misleading "cannot delete movie with screenings".
     * <p>
     * We check explicitly rather than relying on MySQL's FK error because
     * that produces a cryptic DataIntegrityViolationException.
     *
     * @param id Movie's database ID
     */
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }
        if (!screeningRepository.findByMovieId(id).isEmpty()) {
            throw new MovieHasScreeningsException("Cannot delete movie with existing screenings. Delete screenings first.");
        }
        movieRepository.deleteById(id);
        logger.info("admin deleted movie id {}", id);
    }
}
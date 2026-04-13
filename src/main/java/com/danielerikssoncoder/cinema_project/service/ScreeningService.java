package com.danielerikssoncoder.cinema_project.service;

import com.danielerikssoncoder.cinema_project.dto.request.ScreeningRequest;
import com.danielerikssoncoder.cinema_project.dto.response.ScreeningResponse;
import com.danielerikssoncoder.cinema_project.entity.Movie;
import com.danielerikssoncoder.cinema_project.entity.Room;
import com.danielerikssoncoder.cinema_project.entity.Screening;
import com.danielerikssoncoder.cinema_project.exception.ResourceNotFoundException;
import com.danielerikssoncoder.cinema_project.exception.ScreeningHasTicketsException;
import com.danielerikssoncoder.cinema_project.repository.MovieRepository;
import com.danielerikssoncoder.cinema_project.repository.RoomRepository;
import com.danielerikssoncoder.cinema_project.repository.ScreeningRepository;
import com.danielerikssoncoder.cinema_project.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for screenings.
 * <p>
 * GET is available to USER and ADMIN. POST and DELETE require ADMIN (set in SecurityConfig).
 * <p>
 * Listing supports two modes: no movieId returns all screenings,
 * movieId with optional date filters for a specific film.
 */
@Service
@Transactional
public class ScreeningService {

    private static final Logger logger = LoggerFactory.getLogger(ScreeningService.class);

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;

    public ScreeningService(ScreeningRepository screeningRepository,
                            MovieRepository movieRepository,
                            RoomRepository roomRepository,
                            TicketRepository ticketRepository) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Returns all screenings with movie and room details.
     * <p>
     * Used when no movieId query param is provided.
     *
     * @return List of all screenings
     */
    public List<ScreeningResponse> getAllScreenings() {
        return screeningRepository.findAllWithDetails().stream()
                .map(ScreeningResponse::fromEntity)
                .toList();
    }

    /**
     * Returns screenings for a specific movie, with optional date filtering.
     * <p>
     * If date is null, all screenings for the movie are returned.
     *
     * @param movieId Movie ID to filter by
     * @param date Optional date to filter by
     * @return List of matching screenings
     */
    public List<ScreeningResponse> getScreeningsByMovieAndDate(Long movieId, LocalDate date) {

        List<Screening> screenings;

        if (date != null) {
            screenings = screeningRepository.findByMovieIdAndDate(movieId, date);
        } else {
            screenings = screeningRepository.findByMovieId(movieId);
        }

        return screenings.stream()
                .map(ScreeningResponse::fromEntity)
                .toList();
    }

    /**
     * Creates a new screening.
     * <p>
     * The Screening constructor sets availableSeats to room.getMaxGuests() automatically.
     * <p>
     * Throws 404 if the movie or room does not exist.
     *
     * @param request Screening data
     * @return The saved screening entity
     */
    public Screening createScreening(ScreeningRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        Screening screening = new Screening(movie, room, request.getDate(), request.getTime(), request.getPricePerTicket());
        Screening saved = screeningRepository.save(screening);
        logger.info("admin created screening for movie '{}' on {}", movie.getTitle(), request.getDate());
        return saved;
    }

    /**
     * Deletes a screening if no tickets have been sold.
     * <p>
     * Deleting a screening with sold tickets would orphan Ticket rows
     * and destroy the customer's purchase history.
     *
     * @param id Screening's database ID
     */
    public void deleteScreening(Long id) {
        if (!screeningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screening not found with id: " + id);
        }
        if (!ticketRepository.findByScreeningId(id).isEmpty()) {
            throw new ScreeningHasTicketsException("Cannot delete screening with existing tickets.");
        }
        screeningRepository.deleteById(id);
        logger.info("admin deleted screening id {}", id);
    }
}
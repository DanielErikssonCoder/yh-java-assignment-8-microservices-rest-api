package com.danielerikssoncoder.cinema_project.controller;

import com.danielerikssoncoder.cinema_project.dto.request.ScreeningRequest;
import com.danielerikssoncoder.cinema_project.dto.response.ScreeningResponse;
import com.danielerikssoncoder.cinema_project.entity.Screening;
import com.danielerikssoncoder.cinema_project.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles screening endpoints.
 * <p>
 * GET is available to USER and ADMIN. POST and DELETE require ADMIN.
 * <p>
 * Supports optional filtering: no params returns all screenings,
 * ?movieId filters by movie, adding ?date filters by movie and date.
 */
@RestController
@RequestMapping("/api/v1/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    /**
     * GET /api/v1/screenings
     * <p>
     * Returns screenings with optional filtering by movie and date.
     * <p>
     * Both params are optional. @DateTimeFormat is required so Spring
     * can parse "2026-05-01" as a LocalDate.
     *
     * @param movieId Optional: filter by movie
     * @param date Optional: filter by date (format YYYY-MM-DD)
     * @return List of matching screenings (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<ScreeningResponse>> getScreenings(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (movieId != null) {
            return ResponseEntity.ok(screeningService.getScreeningsByMovieAndDate(movieId, date));
        }
        return ResponseEntity.ok(screeningService.getAllScreenings());
    }

    /**
     * POST /api/v1/screenings
     * <p>
     * Creates a new screening. Requires ADMIN.
     * <p>
     * availableSeats is set automatically to the room's max capacity
     * inside the Screening constructor, so it does not need to be sent.
     * <p>
     * Returns 201 Created with a Location header.
     *
     * @param request Screening data (movieId, roomId, date, time, pricePerTicket)
     * @return The created screening (201 Created)
     */
    @PostMapping
    public ResponseEntity<ScreeningResponse> createScreening(@Valid @RequestBody ScreeningRequest request) {
        Screening created = screeningService.createScreening(request);
        ScreeningResponse response = ScreeningResponse.fromEntity(created);
        URI location = URI.create("/api/v1/screenings/" + created.getId());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * DELETE /api/v1/screenings/{screeningId}
     * <p>
     * Deletes a screening. Requires ADMIN.
     * <p>
     * Returns 409 Conflict if tickets have been sold.
     * <p>
     * Returns 204 No Content on success.
     *
     * @param screeningId Database ID of the screening
     * @return 204 No Content
     */
    @DeleteMapping("/{screeningId}")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long screeningId) {
        screeningService.deleteScreening(screeningId);
        return ResponseEntity.noContent().build();
    }
}

package com.danielerikssoncoder.cinema_project.repository;

import com.danielerikssoncoder.cinema_project.entity.Screening;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access for screenings.
 * <p>
 * Most list methods use @EntityGraph to load movie and room in the same query.
 * <p>
 * This is required so ScreeningResponse.fromEntity() can call
 * screening.getMovie().getTitle() without triggering a LazyInitializationException.
 * <p>
 * findByIdForUpdate uses a pessimistic write lock to prevent race conditions
 * when multiple customers try to buy tickets at the same time.
 */
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    /**
     * Returns all screenings with movie and room loaded.
     * <p>
     * An explicit @Query is needed for @EntityGraph to work with findAll semantics.
     */
    @EntityGraph(attributePaths = {"movie", "room"})
    @Query("SELECT s FROM Screening s")
    List<Screening> findAllWithDetails();

    /** Filters by movie and date, loading movie and room eagerly. */
    @EntityGraph(attributePaths = {"movie", "room"})
    List<Screening> findByMovieIdAndDate(Long movieId, LocalDate date);

    /** Filters by movie only, loading movie and room eagerly. */
    @EntityGraph(attributePaths = {"movie", "room"})
    List<Screening> findByMovieId(Long movieId);

    /** Used by RoomService to check for dependencies before deletion. */
    List<Screening> findByRoomId(Long roomId);

    /**
     * Fetches a screening with a pessimistic write lock (SELECT FOR UPDATE).
     * <p>
     * Used by TicketService to prevent race conditions: without the lock, two
     * simultaneous purchases could read the same availableSeats value and both
     * be approved, causing overbooking. The lock is held until the transaction commits.
     *
     * @param id  Screening ID
     * @return    The screening with an exclusive write lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Screening s WHERE s.id = :id")
    Optional<Screening> findByIdForUpdate(@Param("id") Long id);
}
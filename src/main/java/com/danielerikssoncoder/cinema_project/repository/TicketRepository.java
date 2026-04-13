package com.danielerikssoncoder.cinema_project.repository;

import com.danielerikssoncoder.cinema_project.entity.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for tickets.
 * <p>
 * findByCustomerId loads the full object graph needed for TicketResponse:
 * screening.movie (title), screening.room (room name) and customer.
 * <p>
 * Dot notation ("screening.movie") works for nested paths in @EntityGraph.
 * <p>
 * findByScreeningId is only used by ScreeningService to check whether
 * any tickets exist before deleting a screening, so no EntityGraph is needed.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Returns a customer's tickets with the full screening, movie and room graph loaded. */
    @EntityGraph(attributePaths = {"screening.movie", "screening.room", "customer"})
    List<Ticket> findByCustomerId(Long customerId);

    /** Used by ScreeningService to check for sold tickets before deletion. */
    List<Ticket> findByScreeningId(Long screeningId);
}
package com.danielerikssoncoder.cinema_project.repository;

import com.danielerikssoncoder.cinema_project.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for bookings.
 * <p>
 * findByCustomerId uses @EntityGraph to load customer and room in a single JOIN query.
 * <p>
 * Without it, each call to booking.getCustomer() or booking.getRoom()
 * on a LAZY relation would trigger a separate query (the N+1 problem).
 * <p>
 * With 10 bookings that would be 21 queries instead of 1.
 * <p>
 * findByRoomId is only used to check for dependencies before deleting a room,
 * so no EntityGraph is needed there.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Returns all bookings for a customer, loading customer and room eagerly. */
    @EntityGraph(attributePaths = {"customer", "room"})
    List<Booking> findByCustomerId(Long customerId);

    /** Used by RoomService to check for dependencies before deletion. */
    List<Booking> findByRoomId(Long roomId);
}
package com.danielerikssoncoder.cinema_project.repository;

import com.danielerikssoncoder.cinema_project.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for rooms.
 * <p>
 * Standard JpaRepository methods are sufficient.
 * <p>
 * existsById() is used in RoomService for an early 404 check, same pattern
 * as MovieRepository.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {
}
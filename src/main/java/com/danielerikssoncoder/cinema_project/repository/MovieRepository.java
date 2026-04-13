package com.danielerikssoncoder.cinema_project.repository;

import com.danielerikssoncoder.cinema_project.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for movies.
 * <p>
 * Standard JpaRepository methods are sufficient.
 * <p>
 * existsById() is used in MovieService for an early 404 check before
 * the screening dependency check, so we give a clear error in both cases.
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {
}

package com.danielerikssoncoder.cinema_project.repository;

import com.danielerikssoncoder.cinema_project.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for addresses.
 * <p>
 * Standard JpaRepository methods are sufficient here since addresses
 * are always managed via their owning customer in CustomerService.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {
}

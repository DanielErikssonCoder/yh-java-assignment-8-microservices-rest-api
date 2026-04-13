package com.danielerikssoncoder.cinema_project.service;

import com.danielerikssoncoder.cinema_project.dto.request.RoomRequest;
import com.danielerikssoncoder.cinema_project.dto.response.RoomResponse;
import com.danielerikssoncoder.cinema_project.entity.Room;
import com.danielerikssoncoder.cinema_project.exception.ResourceNotFoundException;
import com.danielerikssoncoder.cinema_project.exception.RoomHasBookingsException;
import com.danielerikssoncoder.cinema_project.exception.RoomHasScreeningsException;
import com.danielerikssoncoder.cinema_project.repository.BookingRepository;
import com.danielerikssoncoder.cinema_project.repository.RoomRepository;
import com.danielerikssoncoder.cinema_project.repository.ScreeningRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for room management. ADMIN only.
 * <p>
 * A room is used both by Screenings and Bookings, so deletion requires checking both dependency types.
 */
@Service
@Transactional
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final ScreeningRepository screeningRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository,
                       ScreeningRepository screeningRepository,
                       BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.screeningRepository = screeningRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Returns all rooms as DTOs.
     *
     * @return List of all rooms
     */
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::fromEntity)
                .toList();
    }

    /**
     * Returns a specific room by ID.
     * <p>
     * Throws 404 if not found.
     *
     * @param id Room's database ID
     * @return The room as a DTO
     */
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return RoomResponse.fromEntity(room);
    }

    /**
     * Creates a new room.
     *
     * @param request Room data (name, maxGuests, technicalEquipment)
     * @return The saved room entity
     */
    public Room createRoom(RoomRequest request) {
        Room room = new Room(request.getName(), request.getMaxGuests(), request.getTechnicalEquipment());
        Room saved = roomRepository.save(room);
        logger.info("admin created room '{}'", saved.getName());
        return saved;
    }

    /**
     * Updates all fields on a room (PUT semantics).
     * <p>
     * Updates the existing entity to preserve the ID and avoid creating a new row.
     *
     * @param id Room's database ID
     * @param request New room data
     * @return The updated room as a DTO
     */
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setName(request.getName());
        room.setMaxGuests(request.getMaxGuests());
        room.setTechnicalEquipment(request.getTechnicalEquipment());
        Room updated = roomRepository.save(room);
        logger.info("admin updated room id {}", id);
        return RoomResponse.fromEntity(updated);
    }

    /**
     * Deletes a room if neither screenings nor bookings are linked to it.
     * <p>
     * Order: existence check, then screenings, then bookings, then delete.
     * <p>
     * Screenings are checked first as the more common dependency.
     * <p>
     * Separate exceptions per type give specific error messages.
     *
     * @param id Room's database ID
     */
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        if (!screeningRepository.findByRoomId(id).isEmpty()) {
            throw new RoomHasScreeningsException("Cannot delete room with existing screenings. Delete screenings first.");
        }
        if (!bookingRepository.findByRoomId(id).isEmpty()) {
            throw new RoomHasBookingsException("Cannot delete room with existing bookings. Delete bookings first.");
        }
        roomRepository.deleteById(id);
        logger.info("admin deleted room id {}", id);
    }
}
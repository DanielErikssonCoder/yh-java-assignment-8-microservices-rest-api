package com.danielerikssoncoder.cinema_project.controller;

import com.danielerikssoncoder.cinema_project.dto.request.RoomRequest;
import com.danielerikssoncoder.cinema_project.dto.response.RoomResponse;
import com.danielerikssoncoder.cinema_project.entity.Room;
import com.danielerikssoncoder.cinema_project.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * Handles room management endpoints. ADMIN only.
 * <p>
 * Full CRUD: list, get, create, update, delete.
 */
@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * GET /api/v1/rooms
     * <p>
     * Returns all rooms.
     *
     * @return List of all rooms (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * <p>
     * Returns a specific room by ID.
     * <p>
     * Returns 404 if not found.
     *
     * @param roomId Database ID of the room
     * @return The room (200 OK) or 404
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    /**
     * POST /api/v1/rooms
     * <p>
     * Creates a new room.
     * <p>
     * Returns 201 Created with a Location header.
     *
     * @param request Room data (name, maxGuests, technicalEquipment)
     * @return The created room (201 Created)
     */
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        Room created = roomService.createRoom(request);
        RoomResponse response = RoomResponse.fromEntity(created);
        URI location = URI.create("/api/v1/rooms/" + created.getId());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * PUT /api/v1/rooms/{roomId}
     * <p>
     * Updates a room. PUT semantics: all fields are replaced.
     * <p>
     * Returns 404 if not found.
     *
     * @param roomId   Database ID of the room
     * @param request  New room data (all fields required)
     * @return         The updated room (200 OK)
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * <p>
     * Deletes a room.
     * <p>
     * Returns 409 Conflict if the room has screenings or bookings.
     * <p>
     * Returns 204 No Content on success.
     *
     * @param roomId Database ID of the room
     * @return 204 No Content
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}

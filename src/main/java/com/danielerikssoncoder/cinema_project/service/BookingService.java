package com.danielerikssoncoder.cinema_project.service;

import com.danielerikssoncoder.cinema_project.dto.request.BookingRequest;
import com.danielerikssoncoder.cinema_project.dto.request.BookingUpdateRequest;
import com.danielerikssoncoder.cinema_project.dto.response.BookingResponse;
import com.danielerikssoncoder.cinema_project.entity.Booking;
import com.danielerikssoncoder.cinema_project.entity.Customer;
import com.danielerikssoncoder.cinema_project.entity.Room;
import com.danielerikssoncoder.cinema_project.exception.ResourceNotFoundException;
import com.danielerikssoncoder.cinema_project.exception.RoomCapacityExceededException;
import com.danielerikssoncoder.cinema_project.repository.BookingRepository;
import com.danielerikssoncoder.cinema_project.repository.CustomerRepository;
import com.danielerikssoncoder.cinema_project.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Business logic for bookings.
 * <p>
 * A booking reserves an entire room at a fixed price of 5000 SEK,
 * converted to USD via the shared library.
 */
@Service
@Transactional
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    // Fixed price for reserving a room
    private static final double ROOM_BOOKING_PRICE_SEK = 5000.0;

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final RoomRepository roomRepository;
    private final CurrencyService currencyService;

    public BookingService(BookingRepository bookingRepository,
                          CustomerRepository customerRepository,
                          RoomRepository roomRepository,
                          CurrencyService currencyService) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.currencyService = currencyService;
    }

    /**
     * Returns all bookings for a customer as DTOs.
     *
     * @param customerId Customer's database ID
     * @return List of the customer's bookings
     */
    public List<BookingResponse> getBookingsByCustomerId(Long customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    /**
     * Creates a new booking after validating room capacity.
     * <p>
     * Throws 404 if the customer or room does not exist.
     * <p>
     * Throws 400 if numberOfGuests exceeds the room's capacity.
     *
     * @param request  Booking data (customerId set from JWT by the controller)
     * @return         The saved booking entity
     */
    public Booking createBooking(BookingRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        if (request.getNumberOfGuests() > room.getMaxGuests()) {
            throw new RoomCapacityExceededException("Number of guests exceeds room capacity of " + room.getMaxGuests());
        }

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setRoom(room);
        booking.setDate(request.getDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setPerformance(request.getPerformance());
        booking.setTechnicalEquipment(request.getTechnicalEquipment());
        booking.setTotalPriceSek(ROOM_BOOKING_PRICE_SEK);
        booking.setTotalPriceUsd(currencyService.convertSekToUsd(ROOM_BOOKING_PRICE_SEK));

        Booking saved = bookingRepository.save(booking);
        logger.info("user created booking id {} for room '{}'", saved.getId(), room.getName());
        return saved;
    }

    /**
     * Updates date and/or technical equipment on a booking.
     * <p>
     * Takes an already-fetched Booking entity to avoid an extra database call.
     * <p>
     * The controller fetches it for the ownership check and we reuse the same object.
     * <p>
     * Null fields in the request are ignored, only set fields are updated.
     *
     * @param booking The existing booking (already fetched by the controller)
     * @param request Fields to update (all optional)
     * @return The updated booking as a DTO
     */
    public BookingResponse updateBooking(Booking booking, BookingUpdateRequest request) {
        if (request.getDate() != null) {
            booking.setDate(request.getDate());
        }
        if (request.getTechnicalEquipment() != null) {
            booking.setTechnicalEquipment(request.getTechnicalEquipment());
        }
        Booking updated = bookingRepository.save(booking);
        logger.info("user updated booking id {}", booking.getId());
        return BookingResponse.fromEntity(updated);
    }

    /**
     * Returns a booking as a raw entity for internal use.
     * <p>
     * Used by BookingController before calling AuthService.verifyOwnership().
     *
     * @param bookingId Booking ID
     * @return The booking entity
     */
    public Booking getBookingEntity(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));
    }
}
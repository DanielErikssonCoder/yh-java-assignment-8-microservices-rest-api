package com.danielerikssoncoder.cinema_project.dto.response;

import com.danielerikssoncoder.cinema_project.entity.Booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response representation of a booking.
 * <p>
 * Relations are flattened to simple fields instead of nesting full objects:
 * <p>
 * customerId + customerName instead of a full CustomerResponse, and roomId + roomName instead of a full RoomResponse.
 * <p>
 * Price is included in both SEK and USD as required by the assignment.
 */
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long roomId;
    private String roomName;
    private int numberOfGuests;
    private LocalDate date;
    private String performance;
    private String technicalEquipment;
    private double totalPriceSek;
    private double totalPriceUsd;
    private LocalDateTime createdAt;

    public BookingResponse() {}

     /**
     * Converts a Booking entity to a BookingResponse DTO.
     * <p>
     * Must be called inside a @Transactional method so LAZY relations
     * (customer, room) are guaranteed to be loaded.
     *
     * @param booking Entity to convert
     * @return DTO ready for JSON serialization
     */
    public static BookingResponse fromEntity(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setCustomerId(booking.getCustomer().getId());
        response.setCustomerName(booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
        response.setRoomId(booking.getRoom().getId());
        response.setRoomName(booking.getRoom().getName());
        response.setNumberOfGuests(booking.getNumberOfGuests());
        response.setDate(booking.getDate());
        response.setPerformance(booking.getPerformance());
        response.setTechnicalEquipment(booking.getTechnicalEquipment());
        response.setTotalPriceSek(booking.getTotalPriceSek());
        response.setTotalPriceUsd(booking.getTotalPriceUsd());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    public String getTechnicalEquipment() {
        return technicalEquipment;
    }

    public void setTechnicalEquipment(String technicalEquipment) {
        this.technicalEquipment = technicalEquipment;
    }

    public double getTotalPriceSek() {
        return totalPriceSek;
    }

    public void setTotalPriceSek(double totalPriceSek) {
        this.totalPriceSek = totalPriceSek;
    }

    public double getTotalPriceUsd() {
        return totalPriceUsd;
    }

    public void setTotalPriceUsd(double totalPriceUsd) {
        this.totalPriceUsd = totalPriceUsd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

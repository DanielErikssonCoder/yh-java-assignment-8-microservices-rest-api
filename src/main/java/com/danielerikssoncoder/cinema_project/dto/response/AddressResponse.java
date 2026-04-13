package com.danielerikssoncoder.cinema_project.dto.response;

import com.danielerikssoncoder.cinema_project.entity.Address;

/**
 * Response representation of an address.
 * <p>
 * Response DTOs control exactly what the client sees in JSON.
 * <p>
 * The customer reference is intentionally excluded to avoid circular data.
 * <p>
 * fromEntity() is a static factory method: conversion logic lives in the DTO,
 * not in the service layer.
 */
public class AddressResponse {

    private Long id;
    private String street;
    private String city;
    private String zipCode;
    private String country;

    public AddressResponse() {}

    /**
     * Converts an Address entity to an AddressResponse DTO.
     * @param address Entity to convert
     * @return DTO ready for JSON serialization
     */
    public static AddressResponse fromEntity(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setZipCode(address.getZipCode());
        response.setCountry(address.getCountry());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

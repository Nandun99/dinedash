package com.dinedash.auth.dto;

import com.dinedash.auth.domain.Address;

public record AddressResponse(
        Long id,
        String label,
        String line1,
        String line2,
        String city,
        Double latitude,
        Double longitude,
        boolean defaultAddress
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(address.getId(), address.getLabel(), address.getLine1(), address.getLine2(),
                address.getCity(), address.getLatitude(), address.getLongitude(), address.isDefaultAddress());
    }
}

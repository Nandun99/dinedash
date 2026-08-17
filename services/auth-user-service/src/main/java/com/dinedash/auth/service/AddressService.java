package com.dinedash.auth.service;

import com.dinedash.auth.domain.Address;
import com.dinedash.auth.dto.AddressRequest;
import com.dinedash.auth.dto.AddressResponse;
import com.dinedash.auth.exception.BadRequestException;
import com.dinedash.auth.exception.NotFoundException;
import com.dinedash.auth.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    static final int MAX_ADDRESSES_PER_USER = 10;

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long userId) {
        return addressRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest request) {
        List<Address> existing = addressRepository.findByUserIdOrderByIdAsc(userId);
        if (existing.size() >= MAX_ADDRESSES_PER_USER) {
            throw new BadRequestException("You can save up to " + MAX_ADDRESSES_PER_USER + " addresses");
        }
        Address address = new Address();
        address.setUser(userService.findUser(userId));
        apply(address, request);
        boolean makeDefault = request.defaultAddress() || existing.isEmpty();
        if (makeDefault) {
            addressRepository.clearDefaultForUser(userId);
        }
        address.setDefaultAddress(makeDefault);
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long userId, Long addressId, AddressRequest request) {
        Address address = find(userId, addressId);
        apply(address, request);
        if (request.defaultAddress() && !address.isDefaultAddress()) {
            addressRepository.clearDefaultForUser(userId);
            address.setDefaultAddress(true);
        }
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        addressRepository.delete(find(userId, addressId));
    }

    private Address find(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
    }

    private static void apply(Address address, AddressRequest request) {
        address.setLabel(request.label().trim());
        address.setLine1(request.line1().trim());
        address.setLine2(request.line2());
        address.setCity(request.city().trim());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
    }
}

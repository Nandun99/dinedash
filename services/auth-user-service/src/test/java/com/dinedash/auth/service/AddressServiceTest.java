package com.dinedash.auth.service;

import com.dinedash.auth.domain.Address;
import com.dinedash.auth.domain.User;
import com.dinedash.auth.dto.AddressRequest;
import com.dinedash.auth.dto.AddressResponse;
import com.dinedash.auth.exception.BadRequestException;
import com.dinedash.auth.exception.NotFoundException;
import com.dinedash.auth.repository.AddressRepository;
import com.dinedash.common.security.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    private static final AddressRequest HOME =
            new AddressRequest("Home", "12 Galle Road", null, "Colombo", 6.9, 79.85, false);

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private AddressService addressService;

    @Test
    void firstAddressBecomesDefault() {
        when(addressRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of());
        when(userService.findUser(1L)).thenReturn(new User("a@b.lk", "h", "A", null, Role.CUSTOMER));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse response = addressService.create(1L, HOME);

        assertThat(response.defaultAddress()).isTrue();
        verify(addressRepository).clearDefaultForUser(1L);
    }

    @Test
    void rejectsMoreThanTheAddressLimit() {
        when(addressRepository.findByUserIdOrderByIdAsc(1L))
                .thenReturn(Collections.nCopies(AddressService.MAX_ADDRESSES_PER_USER, new Address()));

        assertThatThrownBy(() -> addressService.create(1L, HOME)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void cannotTouchAnotherUsersAddress() {
        when(addressRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.delete(1L, 5L)).isInstanceOf(NotFoundException.class);
    }
}

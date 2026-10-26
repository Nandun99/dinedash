package com.dinedash.delivery.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.delivery.dto.DriverDtos.DriverResponse;
import com.dinedash.delivery.dto.DriverDtos.LocationRequest;
import com.dinedash.delivery.dto.DriverDtos.ProfileRequest;
import com.dinedash.delivery.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers/me")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public DriverResponse me(@AuthenticationPrincipal CurrentUser user) {
        return driverService.me(user.id());
    }

    @PutMapping
    public DriverResponse saveProfile(@AuthenticationPrincipal CurrentUser user,
                                      @Valid @RequestBody ProfileRequest request) {
        return driverService.saveProfile(user.id(), request);
    }

    @PostMapping("/online")
    public DriverResponse online(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody LocationRequest location) {
        return driverService.goOnline(user.id(), location);
    }

    @PostMapping("/offline")
    public DriverResponse offline(@AuthenticationPrincipal CurrentUser user) {
        return driverService.goOffline(user.id());
    }

    @PutMapping("/location")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void location(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody LocationRequest location) {
        driverService.updateLocation(user.id(), location);
    }
}

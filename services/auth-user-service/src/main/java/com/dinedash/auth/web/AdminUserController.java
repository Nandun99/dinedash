package com.dinedash.auth.web;

import com.dinedash.auth.dto.UserResponse;
import com.dinedash.auth.service.UserService;
import com.dinedash.common.security.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public Page<UserResponse> list(@RequestParam(required = false) Role role,
                                   @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return userService.list(role, pageable);
    }

    @PatchMapping("/{userId}/enabled")
    public UserResponse setEnabled(@PathVariable Long userId, @RequestParam boolean value) {
        return userService.setEnabled(userId, value);
    }
}

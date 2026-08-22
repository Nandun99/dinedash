package com.dinedash.auth.service;

import com.dinedash.auth.domain.User;
import com.dinedash.auth.dto.UpdateProfileRequest;
import com.dinedash.auth.dto.UserResponse;
import com.dinedash.auth.exception.NotFoundException;
import com.dinedash.auth.repository.UserRepository;
import com.dinedash.auth.security.RefreshTokenService;
import com.dinedash.common.security.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Role role, Pageable pageable) {
        Page<User> page = role == null ? userRepository.findAll(pageable) : userRepository.findByRole(role, pageable);
        return page.map(UserResponse::from);
    }

    @Transactional
    public UserResponse setEnabled(Long userId, boolean enabled) {
        User user = findUser(userId);
        user.setEnabled(enabled);
        if (!enabled) {
            refreshTokenService.revokeAll(userId);
        }
        return UserResponse.from(user);
    }

    User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

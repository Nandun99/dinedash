package com.dinedash.auth.service;

import com.dinedash.auth.domain.User;
import com.dinedash.auth.dto.UpdateProfileRequest;
import com.dinedash.auth.dto.UserResponse;
import com.dinedash.auth.exception.NotFoundException;
import com.dinedash.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

    User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

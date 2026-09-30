package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.UserResponse;
import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.exception.UserNotFoundException;
import com.restaurant.rest_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional
    public UserResponse updateRole(Long id, Role newRole){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setRole(newRole);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }
}

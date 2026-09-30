package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.UserResponse;
import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.exception.UserNotFoundException;
import com.restaurant.rest_api.fixtures.UserFixture;
import com.restaurant.rest_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void updateRole(){
        User user = UserFixture.buildUser();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.updateRole(user.getId(), Role.WAITER);

        assertEquals(Role.WAITER, result.role());
        assertEquals(Role.WAITER, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    public void updateRole_shouldThrowException_whenUserNotFound(){
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateRole(999L, Role.WAITER);
        });
        verify(userRepository, never()).save(any(User.class));
    }
}

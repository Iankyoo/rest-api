package com.restaurant.rest_api.Service;

import com.restaurant.rest_api.dto.RegisterRequest;
import com.restaurant.rest_api.dto.UserResponse;
import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.repository.UserRepository;
import com.restaurant.rest_api.security.JwtService;
import com.restaurant.rest_api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveRegistrarUsuarioComSucesso() {
        // Arrange
        RegisterRequest request = new RegisterRequest("João", "joao@email.com", "senha123");

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senhaHasheada");

        User userSalvo = User.builder()
                .id(1L)
                .name("João")
                .email("joao@email.com")
                .password("senhaHasheada")
                .role(Role.CUSTOMER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(userSalvo);

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertEquals("João", response.name());
        assertEquals("joao@email.com", response.email());
        verify(userRepository).save(any(User.class));
    }
}

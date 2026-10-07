package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.AuthResponse;
import com.restaurant.rest_api.dto.LoginRequest;
import com.restaurant.rest_api.dto.RegisterRequest;
import com.restaurant.rest_api.dto.UserResponse;
import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.exception.EmailAlreadyExistsException;
import com.restaurant.rest_api.exception.InvalidCredentialsException;
import com.restaurant.rest_api.fixtures.UserFixture;
import com.restaurant.rest_api.repository.UserRepository;
import com.restaurant.rest_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    public void register_shouldCreateCustomerWithEncodedPassword(){
        RegisterRequest request = new RegisterRequest("testName", "test@email.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encodedPassword", captor.getValue().getPassword());
        assertEquals(Role.CUSTOMER, captor.getValue().getRole());
        assertEquals("test@email.com", result.email());
        assertEquals(Role.CUSTOMER, result.role());
    }

    @Test
    public void register_shouldThrowException_whenEmailAlreadyExists(){
        RegisterRequest request = new RegisterRequest("testName", "test@email.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(UserFixture.buildUser()));

        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.register(request);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void login_shouldReturnToken_whenCredentialsAreValid(){
        User user = UserFixture.buildUser();
        LoginRequest request = new LoginRequest(user.getEmail(), "password123");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse result = authService.login(request);

        assertEquals("jwt-token", result.token());
        assertEquals(user.getId(), result.userId());
        assertEquals(user.getRole(), result.role());
    }

    @Test
    public void login_shouldThrowException_whenPasswordIsWrong(){
        User user = UserFixture.buildUser();
        LoginRequest request = new LoginRequest(user.getEmail(), "wrongPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(request);
        });
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    public void login_shouldThrowException_whenEmailNotFound(){
        LoginRequest request = new LoginRequest("unknown@email.com", "password123");

        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(request);
        });
        verify(jwtService, never()).generateToken(any(User.class));
    }
}

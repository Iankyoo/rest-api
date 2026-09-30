package com.restaurant.rest_api.config;

import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.fixtures.UserFixture;
import com.restaurant.rest_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void run_shouldCreateAdmin_whenAdminDoesNotExist(){
        AdminSeeder seeder = new AdminSeeder(userRepository, passwordEncoder, "admin@test.com", "admin123");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");

        seeder.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("admin@test.com", captor.getValue().getEmail());
        assertEquals("encodedPassword", captor.getValue().getPassword());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    public void run_shouldNotCreateAdmin_whenAdminAlreadyExists(){
        AdminSeeder seeder = new AdminSeeder(userRepository, passwordEncoder, "admin@test.com", "admin123");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(UserFixture.buildUser()));

        seeder.run();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void run_shouldNotCreateAdmin_whenCredentialsAreNotConfigured(){
        AdminSeeder seeder = new AdminSeeder(userRepository, passwordEncoder, "", "");

        seeder.run();

        verifyNoInteractions(userRepository);
    }
}

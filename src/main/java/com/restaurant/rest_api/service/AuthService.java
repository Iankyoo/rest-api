package com.restaurant.rest_api.service;

import com.restaurant.rest_api.dto.AuthResponse;
import com.restaurant.rest_api.dto.LoginRequest;
import com.restaurant.rest_api.dto.RegisterRequest;
import com.restaurant.rest_api.dto.UserResponse;
import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.exception.EmailAlreadyExistsException;
import com.restaurant.rest_api.exception.InvalidCredentialsException;
import com.restaurant.rest_api.repository.UserRepository;
import com.restaurant.rest_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public UserResponse register(RegisterRequest request){
        if (userRepository.findByEmail(request.email()).isPresent()){
            throw new EmailAlreadyExistsException(request.email());
        }

        User newUser = User.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();

        User saved = userRepository.save(newUser);
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(request.password(),user.getPassword())){
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}

package com.restaurant.rest_api.config;

import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.email:}") String adminEmail,
            @Value("${admin.password:}") String adminPassword
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args){
        if (adminEmail.isBlank() || adminPassword.isBlank()){
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD não configurados, admin inicial não foi criado");
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent()){
            return;
        }

        User admin = User.builder()
                .name("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Admin inicial criado: {}", adminEmail);
    }
}

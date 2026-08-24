package com.restaurant.rest_api.Controller;

import com.restaurant.rest_api.repository.UserRepository;
import com.restaurant.rest_api.security.JwtService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class BaseControllerTest {

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected UserRepository userRepository;
}

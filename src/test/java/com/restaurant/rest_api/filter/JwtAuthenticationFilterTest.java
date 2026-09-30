package com.restaurant.rest_api.filter;

import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.fixtures.UserFixture;
import com.restaurant.rest_api.repository.UserRepository;
import com.restaurant.rest_api.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    public void clearContext(){
        SecurityContextHolder.clearContext();
    }

    @Test
    public void doFilter_shouldNotAuthenticate_whenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    public void doFilter_shouldNotAuthenticate_whenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        verify(jwtService, never()).getEmail(anyString());
    }

    @Test
    public void doFilter_shouldAuthenticate_whenTokenIsValid() throws Exception {
        User user = UserFixture.buildUser();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.getEmail("valid-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(user, authentication.getPrincipal());
        assertNotNull(chain.getRequest());
    }

    @Test
    public void doFilter_shouldNotAuthenticate_whenUserNoLongerExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.getEmail("valid-token")).thenReturn("deleted@test.com");
        when(userRepository.findByEmail("deleted@test.com")).thenReturn(Optional.empty());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }
}

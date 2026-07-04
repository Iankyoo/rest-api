package com.restaurant.rest_api.filter;

import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.exception.InvalidTokenException;
import com.restaurant.rest_api.exception.UserNotFoundException;
import com.restaurant.rest_api.repository.UserRepository;
import com.restaurant.rest_api.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHead = request.getHeader("Authorization");

        if (authHead == null || !authHead.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = authHead.substring(7);
        String email = jwtService.getEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null)
            if(jwtService.isTokenValid(token)){
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new InvalidTokenException(email));

                UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        }

}


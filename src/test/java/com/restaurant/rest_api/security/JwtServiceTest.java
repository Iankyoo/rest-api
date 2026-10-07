package com.restaurant.rest_api.security;

import com.restaurant.rest_api.entity.User;
import com.restaurant.rest_api.fixtures.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtServiceTest {

    private static final String SECRET =
            "dGVzdC1zZWNyZXQtb25seS1mb3ItdW5pdC10ZXN0cy1uZXZlci11c2UtaW4tcHJvZHVjdGlvbi0xMjM0NTY3OA==";
    private static final String OTHER_SECRET =
            "b3V0cmEtY2hhdmUtc2VjcmV0YS1kaWZlcmVudGUtcGFyYS10ZXN0YXItYXNzaW5hdHVyYS1pbnZhbGlkYS0xMjM0";

    private JwtService jwtService;

    private JwtService buildService(String secret, long expiration){
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "expiration", expiration);
        return service;
    }

    @BeforeEach
    public void setUp(){
        jwtService = buildService(SECRET, 60000);
    }

    @Test
    public void generateToken_shouldContainUserEmail(){
        User user = UserFixture.buildUser();

        String token = jwtService.generateToken(user);

        assertEquals(user.getEmail(), jwtService.getEmail(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    public void generateToken_shouldContainUserRole(){
        User user = UserFixture.buildUser();

        String token = jwtService.generateToken(user);

        assertEquals(user.getRole().name(), jwtService.extractClaims(token).get("role"));
    }

    @Test
    public void isTokenValid_shouldReturnFalse_whenTokenIsExpired(){
        JwtService expiredService = buildService(SECRET, -1000);
        String token = expiredService.generateToken(UserFixture.buildUser());

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    public void isTokenValid_shouldReturnFalse_whenSignedWithAnotherKey(){
        JwtService otherService = buildService(OTHER_SECRET, 60000);
        String token = otherService.generateToken(UserFixture.buildUser());

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    public void isTokenValid_shouldReturnFalse_whenTokenIsMalformed(){
        assertFalse(jwtService.isTokenValid("not-a-jwt"));
    }
}

package com.campusconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private UserDetails student() {
        return User.builder()
                .username("student1@test.com")
                .password("hashed")
                .authorities("ROLE_STUDENT")
                .build();
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // same shape as the configured base64 secret, injected directly since there's no Spring context here
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "ZHVtbXktZGV2LXNlY3JldC1jaGFuZ2UtbWUtaW4tcHJvZHVjdGlvbi1jYW1wdXNjb25uZWN0LWFpLTEyMzQ1Ng==");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
    }

    @Test
    void generatesTokenAndExtractsTheUsername() {
        String token = jwtService.generateToken(student());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("student1@test.com");
    }

    @Test
    void validTokenIsAcceptedForTheSameUser() {
        UserDetails user = student();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void tokenIsRejectedForADifferentUser() {
        String token = jwtService.generateToken(student());

        UserDetails someoneElse = User.builder()
                .username("other@test.com")
                .password("hashed")
                .authorities("ROLE_STUDENT")
                .build();

        assertThat(jwtService.isTokenValid(token, someoneElse)).isFalse();
    }
}

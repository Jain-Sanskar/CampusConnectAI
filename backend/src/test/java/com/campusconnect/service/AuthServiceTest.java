package com.campusconnect.service;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.exception.EmailAlreadyExistsException;
import com.campusconnect.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test Student");
        req.setEmail("student1@test.com");
        req.setPassword("secret123");
        return req;
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        RegisterRequest req = registerRequest();
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(req.getEmail()).password("hashed").authorities("ROLE_STUDENT").build();
        when(userDetailsService.loadUserByUsername(req.getEmail())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        assertThat(response.getName()).isEqualTo("Test Student");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest req = registerRequest();
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("student1@test.com");
        req.setPassword("secret123");

        User stored = new User("Test Student", req.getEmail(), "hashed", Role.STUDENT);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(stored));
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(req.getEmail()).password("hashed").authorities("ROLE_STUDENT").build();
        when(userDetailsService.loadUserByUsername(req.getEmail())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getName()).isEqualTo("Test Student");
    }

    @Test
    void loginFailsForWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("student1@test.com");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken((UserDetails) any());
    }
}

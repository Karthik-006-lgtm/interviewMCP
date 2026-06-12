package com.interviewprep.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interviewprep.platform.dto.auth.AuthDtos.RegisterRequest;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.security.CustomUserDetailsService;
import com.interviewprep.platform.security.JwtService;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenRevocationService tokenRevocationService;

    private AuthService authService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "ZmFrZV9kZXZfb25seV9zZWNyZXRfZmFrZV9kZXZfb25seV9zZWNyZXRfZmFrZQ==",
                60000L
        );
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                userDetailsService,
                tokenRevocationService
        );
    }

    @SuppressWarnings("null")
    @Test
    void registerCreatesEncodedUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "SecurePass123");
        AtomicReference<User> savedUserReference = new AtomicReference<>();

        when(userRepository.findByEmailIgnoreCase("jane@example.com"))
                .thenAnswer(invocation -> Optional.ofNullable(savedUserReference.get()));
        when(passwordEncoder.encode("SecurePass123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            savedUserReference.set(user);
            return user;
        });

        var response = authService.register(request);

        assertEquals("Jane Doe", response.user().fullName());
        assertEquals("jane@example.com", response.user().email());
        assertEquals(Set.of("USER"), response.user().roles());
        assertEquals("jane@example.com", new JwtService(
                "ZmFrZV9kZXZfb25seV9zZWNyZXRfZmFrZV9kZXZfb25seV9zZWNyZXRfZmFrZQ==",
                60000L
        ).extractUsername(response.access_token()));
        verify(passwordEncoder).encode("SecurePass123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void logoutRevokesBearerToken() {
        var userDetails = org.springframework.security.core.userdetails.User.withUsername("jane@example.com")
                .password("encoded-password")
                .authorities("ROLE_USER")
                .build();
        String token = jwtService.generateToken(userDetails, java.util.Map.of());

        var response = authService.logout("Bearer " + token);

        assertTrue(response.message().contains("revoked"));
        verify(tokenRevocationService).revoke(anyString(), any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        User existingUser = new User();
        existingUser.setEmail("jane@example.com");
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(new RegisterRequest("Jane Doe", "jane@example.com", "SecurePass123"))
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }
}

package com.interviewprep.platform.service;

import com.interviewprep.platform.dto.auth.AuthDtos.AuthResponse;
import com.interviewprep.platform.dto.auth.AuthDtos.LoginRequest;
import com.interviewprep.platform.dto.auth.AuthDtos.LogoutResponse;
import com.interviewprep.platform.dto.auth.AuthDtos.RegisterRequest;
import com.interviewprep.platform.dto.auth.AuthDtos.UserProfileResponse;
import com.interviewprep.platform.entity.User;
import com.interviewprep.platform.entity.enums.UserRole;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.security.CustomUserDetailsService;
import com.interviewprep.platform.security.JwtService;
import java.util.Map;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            TokenRevocationService tokenRevocationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmailIgnoreCase(request.email())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("An account already exists for this email");
                });

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(UserRole.USER));

        User savedUser = userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails, Map.of("roles", savedUser.getRoles().stream().map(Enum::name).toList()));
        return new AuthResponse(token, toProfile(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userDetailsService.loadDomainUserByEmail(request.email());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, Map.of("roles", user.getRoles().stream().map(Enum::name).toList()));
        return new AuthResponse(token, toProfile(user));
    }

    public UserProfileResponse getProfile(String email) {
        return toProfile(userDetailsService.loadDomainUserByEmail(email));
    }

    public User loadUser(String email) {
        return userDetailsService.loadDomainUserByEmail(email);
    }

    public LogoutResponse logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("A valid Bearer token is required to log out");
        }

        String token = authorizationHeader.substring(7);
        tokenRevocationService.revoke(token, jwtService.extractExpiration(token));
        return new LogoutResponse("Logged out successfully. This JWT has been revoked on the server.");
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}

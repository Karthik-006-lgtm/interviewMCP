package com.interviewprep.platform.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 120) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 120) String password
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record UserProfileResponse(
            Long id,
            String fullName,
            String email,
            Set<String> roles,
            Instant createdAt
    ) {
    }

    public record AuthResponse(
            String token,
            UserProfileResponse user
    ) {
    }

    public record LogoutResponse(
            String message
    ) {
    }
}

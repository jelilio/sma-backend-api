package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDto(
    @NotNull @NotBlank
    String name,
    @NotNull @NotBlank @Email
    String email,
    @NotNull @NotBlank
    String password
) {}

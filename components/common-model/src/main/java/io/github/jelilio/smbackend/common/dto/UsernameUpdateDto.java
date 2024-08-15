package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UsernameUpdateDto(
    @NotNull @NotBlank @NotEmpty
    String username
) { }
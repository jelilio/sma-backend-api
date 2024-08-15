package io.github.jelilio.smbackend.usermanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDto(
    @NotNull @NotBlank
    String name
) {
}

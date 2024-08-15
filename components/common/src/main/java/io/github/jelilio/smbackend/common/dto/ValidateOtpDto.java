package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateOtpDto(
    @NotNull @NotBlank
    String email,
    @NotNull @NotBlank
    String otpKey
) {
}

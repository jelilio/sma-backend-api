package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.utils.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ResendOtpDto(
    @NotNull @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    String emailOrUsername
) {
}

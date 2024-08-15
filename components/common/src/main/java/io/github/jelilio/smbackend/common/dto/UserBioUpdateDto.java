package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.utils.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserBioUpdateDto(
    @NotNull @NotBlank @Size(max = 100)
    String name,
    @Size(max = 50, min = 5)
    @Pattern(regexp = Constants.NAME_REGEX)
    String username,
    @Size(max = 500)
    String bio,
    String idNumber,
    LocalDate birthDate
) {
}

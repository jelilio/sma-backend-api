package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.utils.Constants;
import jakarta.validation.constraints.*;

public record RegisterCommunityDto(
    @NotNull @NotBlank @Size(max = 100)
    String name,
    @Size(max = 50, min = 5)
    @NotNull @NotBlank
    @Pattern(regexp = Constants.NAME_REGEX)
    String username,
    @Size(max = 500)
    String bio,
    @NotNull @NotBlank
    String password
) {}

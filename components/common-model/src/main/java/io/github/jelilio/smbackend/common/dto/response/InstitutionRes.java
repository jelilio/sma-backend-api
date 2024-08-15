package io.github.jelilio.smbackend.common.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InstitutionRes(
    @NotNull @NotBlank
    String id,
    String name,
    String description
) {
}

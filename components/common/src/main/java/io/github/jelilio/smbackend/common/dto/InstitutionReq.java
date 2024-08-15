package io.github.jelilio.smbackend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InstitutionReq(
    @Size(min = 5, max = 100)
    @NotNull @NotBlank
    String name,

    @Size(max = 500)
    String description
) {
}

package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.dto.response.InstitutionRes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SchoolReq(
    @Size(min = 2, max = 100)
    @NotNull @NotBlank
    String name,
    @Size(max = 500)
    String description,
    @Valid @NotNull
    InstitutionRes institution
) {
}

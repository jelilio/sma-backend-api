package io.github.jelilio.smbackend.common.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SchoolRes(
    @NotNull @NotBlank
    String id,
    String name,
    InstitutionRes institution,
    String description
) {}

package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategorizerDto(
    @Size(max = 50, min = 3)
    @NotEmpty @NotBlank @NotNull
    String name,
    @NotNull
    Language lang,
    @Size(max = 200, min = 3)
    String description
) {
}

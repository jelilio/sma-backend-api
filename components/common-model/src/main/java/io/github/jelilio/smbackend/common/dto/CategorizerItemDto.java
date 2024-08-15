package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.utils.Constants;
import jakarta.validation.constraints.*;

public record CategorizerItemDto(
    @Size(max = 50, min = 5)
    @NotEmpty @NotBlank @NotNull
    @Pattern(regexp = Constants.NAME_REGEX)
    String name,
    @Size(max = 500, min = 10)
    @NotEmpty @NotBlank @NotNull
    String sentences
) {
}

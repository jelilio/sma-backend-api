package io.github.jelilio.smbackend.common.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModelRes(
    @NotNull @NotBlank
    String id,
    String name,
    String description,
    String source,
    String type,
    String location,
    boolean primary,
    String lang
) {
}

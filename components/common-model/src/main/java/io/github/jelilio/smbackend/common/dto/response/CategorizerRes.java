package io.github.jelilio.smbackend.common.dto.response;

import jakarta.validation.constraints.NotNull;

public record CategorizerRes(
    @NotNull
    String id,
    String name,
    String lang,
    String description
) {
}

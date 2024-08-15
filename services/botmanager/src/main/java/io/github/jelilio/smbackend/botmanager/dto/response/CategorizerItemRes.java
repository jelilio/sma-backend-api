package io.github.jelilio.smbackend.botmanager.dto.response;

import jakarta.validation.constraints.NotNull;

public record CategorizerItemRes(
    @NotNull
    String id,
    String name,
    String sentences,
    CategorizerRes categorizer
) {
}

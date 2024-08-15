package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.dto.response.CategorizerItemRes;
import io.github.jelilio.smbackend.common.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.entity.enumeration.Severity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record BotActionDto(
    @Valid @NotNull
    CategorizerItemRes item,
    @NotNull
    Action action,
    Severity severity,
    String description
) {
}

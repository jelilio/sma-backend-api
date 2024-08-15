package io.github.jelilio.smbackend.botmanager.dto;

import io.github.jelilio.smbackend.botmanager.dto.response.CategorizerItemRes;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Severity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record BotActionDto(
    @Valid @NotNull
    CategorizerItemRes item,
    @NotNull
    Action action,
    @NotNull
    Severity severity,
    String description
) {
}

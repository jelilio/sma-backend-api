package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.dto.response.CategorizerItemRes;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record BotActionDto(
    @Valid @NotNull
    CategorizerItemRes item,
    @NotNull
    Action action,
    String description
) {
}

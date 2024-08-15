package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.common.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.entity.enumeration.Severity;

public record BotActionRes(
    BotActionIdRes id,
    BotRes bot,
    CategorizerItemRes item,
    Action action,
    Severity severity,
    String description
) {

}

package io.github.jelilio.smbackend.common.dto.response;


import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;

public record BotActionRes(
    BotActionIdRes id,
    BotRes bot,
    CategorizerItemRes item,
    Action action
) {

}

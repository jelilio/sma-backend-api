package io.github.jelilio.smbackend.common.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BotRes(
    @NotNull @NotBlank
    String id,
    String name,
    String lang,
    String description,
    CategorizerRes categorizer,
    ModelRes sentence,
    ModelRes language,
    ModelRes tokenizer,
    ModelRes lemmatizer,
    ModelRes postagger
) {
}

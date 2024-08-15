package io.github.jelilio.smbackend.botmanager.dto;


import io.github.jelilio.smbackend.botmanager.dto.response.CategorizerRes;
import io.github.jelilio.smbackend.botmanager.dto.response.ModelRes;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record BotDto(
    @NotEmpty @NotBlank @NotNull
    String name,
    @NotNull
    Language lang,
    String description,
    @Valid @NotNull
    ModelRes language,
    @Valid @NotNull
    ModelRes sentence,
    @Valid @NotNull
    ModelRes tokenizer,
    @Valid @NotNull
    ModelRes postagger,
    @Valid @NotNull
    ModelRes lemmatizer,
    @Valid @NotNull
    CategorizerRes categorizer
) {

  public Set<String> getModelIds() {
    return nonNullSet(language.id(), sentence.id(), tokenizer.id(), postagger.id(), lemmatizer.id()); // doccatId
  }

  private Set<String> nonNullSet(String... items) {
    return Arrays.stream(items).filter(Objects::nonNull).collect(Collectors.toSet());
  }
}

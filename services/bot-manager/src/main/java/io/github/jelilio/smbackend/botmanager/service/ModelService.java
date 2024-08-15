package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.entity.Model;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Set;

public interface ModelService {
  Uni<Model> findById(String id);

  Uni<Model> findByIdAndType(String id, ModelType type);

  Uni<List<Model>> findAll(ModelType type);

  Uni<List<Model>> findAll(ModelType type, Language lang);

  Uni<List<Model>> findAllByIds(Set<String> modelIds);

  Uni<Paged<Model>> findAll(ModelType type, int size, int index);

  Uni<List<String>> breakSentences(String content);

  Multi<String> breakSentencesToList22(String content, Model model);

  Uni<List<String>> breakSentences(String content, Model model);

  List<String> breakSentencesToList(String content, Model model);

  Uni<List<String>> tokenizeSentences(String content, Model model);

  List<String> tokenizeSentencesToList(String content, Model model);

  List<String> detectPOSTags(List<String> tokens, Model model);

  List<String> lemmatizeTokens(List<String> tokens, List<String> posTags, Model model);
}

package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.entity.Model;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import io.github.jelilio.smbackend.botmanager.utils.Paged;

import java.util.List;
import java.util.Set;

public interface ModelService {
  Model findById(String id);

  Model findByIdAndType(String id, ModelType type);

  List<Model> findAll(ModelType type);

  List<Model> findAll(ModelType type, Language lang);

  List<Model> findAllByIds(Set<String> modelIds);

  Paged<Model> findAll(ModelType type, int size, int index);

  List<String> breakSentences(String content);

  List<String> breakSentencesToList22(String content, Model model);

  List<String> breakSentences(String content, Model model);

  List<String> breakSentencesToList(String content, Model model);

  List<String> tokenizeSentences(String content, Model model);

  List<String> tokenizeSentencesToList(String content, Model model);

  List<String> detectPOSTags(List<String> tokens, Model model);

  List<String> lemmatizeTokens(List<String> tokens, List<String> posTags, Model model);
}

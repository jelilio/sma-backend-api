package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.dto.CategorizerDto;
import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import opennlp.tools.doccat.DoccatModel;

import java.util.List;

public interface CategorizerService {
  Categorizer findById(String id);

  List<Categorizer> findAll(Language language);

  Paged<Categorizer> findAll(int size, int index);

  Categorizer create(CategorizerDto dto);

  Categorizer update(String id, CategorizerDto dto);

  void delete(String id);

  void trainModel();

  DoccatModel trainedModel(String lang, List<CategorizerItem> lineItems);
}

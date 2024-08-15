package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.common.dto.CategorizerDto;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Uni;
import opennlp.tools.doccat.DoccatModel;

import java.util.List;

public interface CategorizerService {
  Uni<Categorizer> findById(String id);

  Uni<List<Categorizer>> findAll(Language language);

  Uni<Paged<Categorizer>> findAll(int size, int index);

  Uni<Categorizer> create(CategorizerDto dto);

  Uni<Categorizer> update(String id, CategorizerDto dto);

  Uni<Void> delete(String id);

  Uni<Void> trainModel();

  DoccatModel trainedModel(String lang, List<CategorizerItem> lineItems);

  Uni<DoccatModel> trainedModel22(String lang, List<CategorizerItem> lineItems);
}

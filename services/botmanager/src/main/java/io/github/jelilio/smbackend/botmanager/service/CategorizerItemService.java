package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.utils.Paged;

import java.util.List;

public interface CategorizerItemService {
  CategorizerItem findById(String id);

  CategorizerItem findById(Categorizer categorizer, String id);

  List<CategorizerItem> findAllItems(Categorizer categorizer);

  Paged<CategorizerItem> findAllItems(Categorizer categorizer, int size, int index);

  CategorizerItem create(Categorizer categorizer, CategorizerItemDto dto);

  CategorizerItem  update(Categorizer categorizer, String id, CategorizerItemDto dto);

  void delete(Categorizer categorizer, String id);
}

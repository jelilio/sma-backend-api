package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.common.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CategorizerItemService {
  Uni<CategorizerItem> findById(String id);

  Uni<CategorizerItem> findById(Categorizer categorizer, String id);

  Uni<List<CategorizerItem>> findAllItems(Categorizer categorizer);

  Uni<Paged<CategorizerItem>> findAllItems(Categorizer categorizer, int size, int index);

  Uni<CategorizerItem> create(Categorizer categorizer, CategorizerItemDto dto);

  Uni<CategorizerItem>  update(Categorizer categorizer, String id, CategorizerItemDto dto);

  Uni<Void> delete(Categorizer categorizer, String id);
}

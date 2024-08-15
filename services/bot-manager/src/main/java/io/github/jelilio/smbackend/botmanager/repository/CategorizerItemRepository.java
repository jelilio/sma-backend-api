package io.github.jelilio.smbackend.botmanager.repository;

import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CategorizerItemRepository implements PanacheRepository<CategorizerItem> {

  public Uni<CategorizerItem> findById(String id) {
    return find("id", UUID.fromString(id)).firstResult();
  }
}

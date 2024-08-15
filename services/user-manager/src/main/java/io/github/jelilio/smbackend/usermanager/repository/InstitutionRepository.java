package io.github.jelilio.smbackend.usermanager.repository;

import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class InstitutionRepository implements PanacheRepository<Institution> {
  public Uni<List<Institution>> findOrdered() {
    return find("ORDER BY name").list();
  }

  public Uni<Institution> findById(String id) {
    return find("id = ?1", UUID.fromString(id)).firstResult();
  }

  public  Uni<Boolean> deleteById(String id) {
    return delete("id = ?1", UUID.fromString(id))
        .map(count -> count > 0);
  }

  public Uni<Institution> findByName(String name) {
    return find("name = ?1", name).firstResult();
  }

  public Uni<Long> countByName(String name) {
    return count("name = ?1", name);
  }

  public Uni<Long> countByNameButNotId(String id, String name) {
    return count("id != ?1 and name = ?2", UUID.fromString(id), name);
  }
}

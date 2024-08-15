package io.github.jelilio.smbackend.usermanager.repository;

import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SchoolRepository implements PanacheRepository<School> {
  public Uni<School> findById(String id) {
    return find("id = ?1", UUID.fromString(id)).firstResult();
  }

  public Uni<Long> countByName(String name) {
    return School.count("name = ?1", name);
  }

  public Uni<Long> countByNameAndInstitution(String name, Institution institution) {
    return School.count("name = ?1 and institution = ?2", name, institution);
  }

  public Uni<School> findByName(String name, Institution institution) {
    return School.find("name = ?1 and institution = ?2", name, institution).firstResult();
  }

  public Uni<Long> countByNameButNotId(String id, String name) {
    return School.count("id != ?1 and name = ?2", UUID.fromString(id), name);
  }

  public Uni<Long> countByNameAndInstitutionButNotId(String id, String name, Institution institution) {
    return School.count("id != ?1 and name = ?2 and institution = ?3", UUID.fromString(id), name, institution);
  }
}

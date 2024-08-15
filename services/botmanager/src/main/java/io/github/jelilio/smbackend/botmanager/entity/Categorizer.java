package io.github.jelilio.smbackend.botmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.botmanager.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.botmanager.entity.base.AuditingEntityListener;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.*;

@Entity
@Cacheable
@Table(name = "categorizers")
@EntityListeners(AuditingEntityListener.class)
public class Categorizer extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Column(name = "name", length = 100, unique = true, nullable = false)
  public String name;
  @Column(name = "description", length = 300)
  public String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "lang", length = 5, nullable = false)
  public Language lang;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "categorizer")
  public Set<CategorizerItem> categorizerItems = new HashSet<>();

  public static Optional<Categorizer> findById(String id) {
    return Optional.of(findById(UUID.fromString(id)));
  }

  public static PanacheQuery<Categorizer> findByName(String name) {
    return Categorizer.find("name = ?1", name);
  }

  public static Long countByName(String name) {
    return Categorizer.count("name = ?1", name);
  }

  public static Long countByNameNotId(String id, String name) {
    return Categorizer.count("id != ?1 and name = ?2", UUID.fromString(id), name);
  }

  public static Long countByIdAndName(String id, String name) {
    return Categorizer.count("id =?1 and name = ?2", UUID.fromString(id), name);
  }

  public static PanacheQuery<Categorizer> findByLanguage(Language lang) {
    return Categorizer.find("lang = ?1", lang);
  }

  public Categorizer() {}

  public Categorizer(String name, String description, Language lang) {
    this.name = name;
    this.description = description;
    this.lang = lang;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Categorizer that = (Categorizer) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

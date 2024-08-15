package io.github.jelilio.smbackend.botmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelSource;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Cacheable
@Table(name = "models")
@EntityListeners(AuditingEntityListener.class)
public class Model extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Column(name = "name", length = 100, nullable = false)
  public String name;

  @Column(name = "description", length = 500)
  public String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", length = 100, nullable = false)
  public ModelSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", length = 100, nullable = false)
  public ModelType type;

  @Column(name = "location", nullable = false)
  public String location;

  @Column(name = "base", nullable = false)
  public boolean primary;

  @Enumerated(EnumType.STRING)
  @Column(name = "lang", length = 5)
  public Language lang;

  public static Uni<Model> findById(String id) {
    return Model.findById(UUID.fromString(id));
  }

  public static Uni<Model> findById(String id, ModelType type) {
    return Model.find("id = ?1 and type = ?2", UUID.fromString(id), type).firstResult();
  }

  public static PanacheQuery<Model> find(ModelType type) {
    return Model.find("type = ?1", type);
  }

  public static PanacheQuery<Model> find(ModelType type, String lang) {
    return Model.find("type = ?1 and lang = ?2", type, lang);
  }

  public static PanacheQuery<Model> findByLang(String lang) {
    return Model.find("lang = ?1", lang);
  }

  public static PanacheQuery<Model> find(ModelType type, Language lang) {
    return Model.find("type = ?1 and lang = ?2", type, lang);
  }

  public static PanacheQuery<Model> findByLang(Language lang) {
    return Model.find("lang = ?1", lang);
  }

  public static Uni<List<Model>> findAllByIds(Set<String> ids) {
    Set<UUID> uuids = ids.stream().map(UUID::fromString).collect(Collectors.toSet());
    return Model.find("id IN ?1", uuids).list();
  }

  public static Uni<Model> findPrimary(ModelType type) {
    return Model.find("type = ?1 and primary = true", type).firstResult();
  }

  public Model() {}

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Model model = (Model) o;
    return Objects.equals(id, model.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Model{" +
        "name='" + name + '\'' +
        ", source=" + source +
        ", type=" + type +
        ", location='" + location + '\'' +
        ", primary=" + primary +
        ", lang='" + lang + '\'' +
        '}';
  }
}

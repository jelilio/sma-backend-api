package io.github.jelilio.smbackend.botmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.botmanager.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.botmanager.entity.base.AuditingEntityListener;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

@Entity
@Cacheable
@Table(name = "bots")
@EntityListeners(AuditingEntityListener.class)
public class Bot extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Column(name = "name", length = 100, nullable = false)
  public String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "lang", length = 5, nullable = false)
  public Language lang;

  @Column(name = "description", length = 500)
  public String description;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "categorizer_id", nullable = false)
  public Categorizer categorizer; // category

//  @Fetch(FetchMode.JOIN)
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "bot_models",
      joinColumns = @JoinColumn(name = "bot_id"),
      inverseJoinColumns = @JoinColumn(name = "model_id")
  )
  public Set<Model> models = new HashSet<>();

  public static Optional<Bot> findById(String id) {
    return Optional.of(find("id = ?1", UUID.fromString(id)).singleResult());
  }

  public static Optional<Bot> findByName(String name) {
    return Optional.of(find("name = ?1", name).firstResult());
  }

  public static Long countByName(String name) {
    return count("name = ?1", name);
  }

  public static Long countByNameNotId(String id, String name) {
    return count("id != ?1 and name = ?2", UUID.fromString(id), name);
  }

  public Bot() {}

  public Bot(String name, Language lang, String description, Set<Model> models) {
    this.name = name;
    this.description = description;
    this.lang = lang;
    this.models = models;
  }

  public Bot(String name, Language lang, String description, Categorizer categorizer, Set<Model> models) {
    this.name = name;
    this.description = description;
    this.lang = lang;
    this.categorizer = categorizer;
    this.models = models;
  }

  public Optional<Model> findModel(ModelType type) {
    BiFunction<ModelType, Set<Model>, Optional<Model>> mod = (ModelType t, Set<Model> allModels) ->
        allModels.stream().filter(it -> it.type == t).findFirst();

    return mod.apply(type, models);
  }

  public Model getSentence() {
    return findModel(ModelType.SENTENCE).orElse(null);
  }

  public Model getLanguage() {
    return findModel(ModelType.LANGUAGE).orElse(null);
  }

  public Model getTokenizer() {
    return findModel(ModelType.TOKENIZER).orElse(null);
  }

  public Model getLemmatizer() {
    return findModel(ModelType.LEMMATIZER).orElse(null);
  }

  public Model getPostagger() {
    return findModel(ModelType.POSTAGGER).orElse(null);
  }
}

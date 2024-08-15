package io.github.jelilio.smbackend.botmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.botmanager.entity.base.AbstractSoftDeletableEntity;
import io.github.jelilio.smbackend.botmanager.entity.base.AuditingEntityListener;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "categorizer_items")
@SQLDelete(sql = "UPDATE categorizer_items SET deleted_at = now() WHERE id = $1", check = ResultCheckStyle.COUNT)
////@Where(clause = "deleted_at IS NULL")
//@FilterDef(name = "deletedCategorizerItemsFilter", defaultCondition = "deleted_at IS NULL or deleted_at IS NOT NULL")
////@Filter(name = "deletedCategorizerItemsFilter", condition = "deleted_at IS NULL or deleted_at IS NOT NULL")
@EntityListeners(AuditingEntityListener.class)
public class CategorizerItem extends AbstractSoftDeletableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;
  @Column(name = "name", length = 100, unique = true, nullable = false)
  public String name;
  @Column(name = "sentences", length = 1000, nullable = false)
  public String sentences;
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "categorizer_id", nullable = false)
  public Categorizer categorizer;

  public static Optional<CategorizerItem> findById(String id) {
    return Optional.of(findById(UUID.fromString(id)));
  }

  public static Optional<CategorizerItem> findById(Categorizer categorizer, String id) {
    return Optional.of(find("id = ?1 and categorizer = ?2", UUID.fromString(id), categorizer).firstResult());
  }

  public static Long countByName(Categorizer categorizer, String name) {
    return CategorizerItem.count("name = ?1 and categorizer = ?2", name, categorizer);
  }

  public static Long countByNameNotId(Categorizer categorizer, String id, String name) {
    return CategorizerItem.count("id != ?1 and (name = ?2 and categorizer = ?3)", UUID.fromString(id), name, categorizer);
  }

  public static Long countByIdAndName(String id, String name) {
    return CategorizerItem.count("id =?1 and name = ?2", UUID.fromString(id), name);
  }

  public static PanacheQuery<CategorizerItem> findAllByCategorizer(Categorizer categorizer) {
    return find("categorizer = ?1", Sort.descending("createdDate"), categorizer)
        .filter("nonDeletedEntries");
  }

  public CategorizerItem() { }

  public CategorizerItem(String name, String sentences) {
    this.name = name;
    this.sentences = sentences;
  }

  public CategorizerItem(UUID id, String name, String sentences) {
    this.id = id;
    this.name = name;
    this.sentences = sentences;
  }

  public CategorizerItem(String name, String sentences, Categorizer categorizer) {
    this.name = name;
    this.sentences = sentences;
    this.categorizer = categorizer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CategorizerItem that = (CategorizerItem) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

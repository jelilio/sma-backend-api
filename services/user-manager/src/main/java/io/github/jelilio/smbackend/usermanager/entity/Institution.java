package io.github.jelilio.smbackend.usermanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "institutions")
@EntityListeners(AuditingEntityListener.class)
public class Institution extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Size(max = 100, min = 5)
  @Column(name = "name", length = 100, nullable = false)
  public String name;

  @Size(max = 500)
  @Column(name = "description", length = 500)
  public String description;

  public Institution() {}

  public Institution(String name) {
    this.name = name;
  }

  public Institution(String name, String description) {
    this.name = name;
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Institution that = (Institution) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

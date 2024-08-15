package io.github.jelilio.smbackend.usermanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Entity
@Cacheable
@Table(name = "courses")
@EntityListeners(AuditingEntityListener.class)
public class Course extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Size(max = 100, min = 2)
  @Column(name = "name", length = 100, nullable = false)
  public String name;

  @Size(max = 500)
  @Column(name = "description", length = 500)
  public String description;

  @ManyToOne
  @JoinColumn(name = "school_id")
  public School school;

  public Course () {}

  public Course(String name, String description, School school) {
    this.name = name;
    this.description = description;
    this.school = school;
  }
}

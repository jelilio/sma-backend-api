package io.github.jelilio.smbackend.common.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
public class AbstractAuditingEntity extends PanacheEntityBase implements Serializable {
  @JsonIgnore
  @Column(name = "created_by", updatable = false, nullable = false)
  public String createdBy;

//  @JsonIgnore
  @Column(name = "created_date", updatable  = false, nullable = false)
  public Instant createdDate;

  @JsonIgnore
  @Column(name = "last_modified_by", nullable = false)
  public String lastModifiedBy;

  @JsonIgnore
  @Column(name = "last_modified_date", nullable = false)
  public Instant lastModifiedDate;
}

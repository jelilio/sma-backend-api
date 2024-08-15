package io.github.jelilio.smbackend.usermanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Entity
@Cacheable
@Table(name = "file_imports")
@EntityListeners(AuditingEntityListener.class)
public class FileImport extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Column(name = "name", nullable = false)
  public String name;

  @Column(name = "total_entries")
  public long totalEntries;

  @Column(name = "failed_count")
  public long failedCount;
}

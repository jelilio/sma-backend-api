package io.github.jelilio.smbackend.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.ExecutionStatus;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Severity;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "violations")
@EntityListeners(AuditingEntityListener.class)
public class Violation extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Size(max = 150, min = 5)
  @Column(name = "caption", length = 150)
  public String caption;

  @Column(name = "image_url")
  public String imageUrl;

  @Column(name = "category", length = 100, nullable = false)
  public String category;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", length = 50, nullable = false)
  public Action action;

  @Enumerated(EnumType.STRING)
  @Column(name = "severity", length = 50, nullable = false)
  public Severity severity;

  @Enumerated(EnumType.STRING)
  @Column(name = "execution_status", length = 50, nullable = false)
  public ExecutionStatus executionStatus;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id")
  public User owner;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "post_id")
  public Post post;

  public static PanacheQuery<Violation> find(User owner) {
    return Violation.find("owner = ?1", Sort.descending("createdDate"), owner);
  }

  public static PanacheQuery<Violation> find(Instant startDate, Instant endDate) {
    return Violation.find("createdDate is between ?1 and ?2", Sort.descending("createdDate"), startDate, endDate);
  }

  public static PanacheQuery<Violation> findAllByCreatedAfterDate(Instant startDate) {
    return Violation.find("createdDate >= ?1", startDate);
  }

  public Violation() {}

  public Violation(String category, Severity severity, Action action, String caption, String imageUrl, User owner, Post post) {
    this.category = category;
    this.severity = severity;
    this.action = action;
    this.caption = caption;
    this.imageUrl = imageUrl;
    this.owner = owner;
    this.post = post;

    this.executionStatus = ExecutionStatus.PENDING;
  }
}

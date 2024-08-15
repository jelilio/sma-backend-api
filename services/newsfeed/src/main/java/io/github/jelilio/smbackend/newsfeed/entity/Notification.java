package io.github.jelilio.smbackend.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.NotificationType;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
public class Notification extends AbstractAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Size(max = 150, min = 5)
  @Column(name = "caption", length = 150)
  public String caption;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  public NotificationType type;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id")
  public User owner;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "post_id")
  public Post post;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "initiator_id")
  public User initiator;

  public static PanacheQuery<Notification> find(User owner) {
    return Notification.find("owner = ?1", Sort.descending("createdDate"), owner);
  }

  public static PanacheQuery<Notification> findAllByCreatedAfterDate(Instant startDate) {
    return Notification.find("createdDate >= ?1", startDate);
  }

  public Notification() {}

  public Notification(String caption, NotificationType type) {
    this.caption = caption;
    this.type = type;
  }

  public Notification(String caption, NotificationType type, User owner, User initiator) {
    this.caption = caption;
    this.type = type;
    this.owner = owner;
    this.initiator = initiator;
  }

  public Notification(String caption, NotificationType type, User initiator, Post post) {
    this.caption = caption;
    this.type = type;
    this.initiator = initiator;
    this.post = post;
  }

  public Notification(String caption, NotificationType type, User initiator, Post post, User owner) {
    this.caption = caption;
    this.type = type;
    this.initiator = initiator;
    this.post = post;
    this.owner = owner;
  }

  public Instant getCreatedAt() {
    return this.createdDate;
  }
}

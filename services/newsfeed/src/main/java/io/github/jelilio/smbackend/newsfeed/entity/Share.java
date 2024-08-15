package io.github.jelilio.smbackend.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.newsfeed.entity.key.ShareId;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Cacheable
@Table(name = "share")
@EntityListeners(AuditingEntityListener.class)
public class Share extends AbstractAuditingEntity {
  @Id
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public ShareId id;

  @ManyToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  public User sharedBy;

  @ManyToOne
  @JoinColumn(name = "post_id", insertable = false, updatable = false)
  public Post post;

  @Column(name = "liked")
  public boolean liked;

  public Share() {}

  public Share(User user, Post post) {
    super();
    this.id = new ShareId(user.id, post.id);
  }

  public static Uni<Long> countById(ShareId id) {
    return Share.count("id = ?1", id);
  }

  public static Uni<Share> findById(ShareId id) {
    return Share.find("id = ?1", id).firstResult();
  }
}

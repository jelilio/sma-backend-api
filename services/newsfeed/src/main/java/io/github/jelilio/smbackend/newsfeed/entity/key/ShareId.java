package io.github.jelilio.smbackend.newsfeed.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ShareId implements Serializable {
  @Column(name = "user_id")
  public UUID userId;
  @Column(name = "post_id")
  public UUID postId;

  public ShareId() {}

  public ShareId(UUID userId, UUID postId) {
    this.userId = userId;
    this.postId = postId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ShareId share = (ShareId) o;
    return Objects.equals(userId, share.userId) && Objects.equals(postId, share.postId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, postId);
  }
}

package io.github.jelilio.smbackend.newsfeed.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class FollowId implements Serializable {
  @Column(name = "user_id")
  public UUID userId;
  @Column(name = "other_user_id")
  public UUID otherUserId;

  public FollowId() {}

  public FollowId(UUID userId, UUID otherUserId) {
    this.userId = userId;
    this.otherUserId = otherUserId;
  }

  public FollowId(String userId, String otherUserId) {
    this.userId = UUID.fromString(userId);
    this.otherUserId = UUID.fromString(otherUserId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FollowId followId = (FollowId) o;
    return Objects.equals(userId, followId.userId) && Objects.equals(otherUserId, followId.otherUserId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, otherUserId);
  }
}

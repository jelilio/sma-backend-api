package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RegisterForReflection
public class Following extends FollowPro {
  public Following(
      UUID id, String name, String email, String username, String avatarUrl, String avatarType,
      UserType userType, Instant verifiedDate, Instant followingDate, boolean itsFollowing, boolean youFollowing) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.username = username;
    this.avatarUrl = avatarUrl;
    this.avatarType = avatarType;
    this.userType = userType;
    this.verifiedDate = verifiedDate;
    this.followingDate = followingDate;
    this.itsFollowing = itsFollowing;
    this.youFollowing = youFollowing;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Following follower = (Following) o;
    return Objects.equals(id, follower.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id);
  }
}

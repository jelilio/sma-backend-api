package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RegisterForReflection
public class FollowingCommunityClub extends FollowPro {
  public Long memberCount;

  public FollowingCommunityClub(
      UUID id, String name, String email, String username, String avatarUrl, String avatarType,
      UserType userType, Instant verifiedDate, Instant followingDate, Long memberCount, boolean itsFollowing, boolean youFollowing) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.username = username;
    this.avatarUrl = avatarUrl;
    this.avatarType = avatarType;
    this.userType = userType;
    this.verifiedDate = verifiedDate;
    this.followingDate = followingDate;
    this.memberCount = memberCount;
    this.itsFollowing = itsFollowing;
    this.youFollowing = youFollowing;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    FollowingCommunityClub follower = (FollowingCommunityClub) o;
    return Objects.equals(id, follower.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id);
  }
}

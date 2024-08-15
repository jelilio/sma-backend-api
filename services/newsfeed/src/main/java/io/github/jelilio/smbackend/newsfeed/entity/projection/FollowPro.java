package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class FollowPro {
  public UUID id;
  public String name;
  public String email;
  public String username;
  public String avatarUrl;
  public String avatarType;
  public UserType userType;
  public Instant verifiedDate;
  public Instant followingDate;
  public boolean youFollowing;
  public boolean itsFollowing;


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FollowPro followPro = (FollowPro) o;
    return Objects.equals(id, followPro.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}

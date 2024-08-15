package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.Instant;

public record FollowRes(
    String id,
    String name,
    String email,
    String username,
    String avatarUrl,
    String avatarType,
    UserType userType,
    Instant verifiedDate,
    Instant followingDate,
    boolean youFollowing,
    boolean itsFollowing,
    boolean itsPending
) {
  boolean isVerified() {
    return verifiedDate != null;
  }
}

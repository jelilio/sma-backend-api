package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.Instant;

public record FollowComClubRes(
    String id,
    String name,
    String email,
    String username,
    String avatarUrl,
    String avatarType,
    UserType userType,
    Instant verifiedDate,
    Instant followingDate,
    Long memberCount,
    boolean youFollowing,
    boolean itsFollowing,
    boolean itsPending
) {
  boolean isVerified() {
    return verifiedDate != null;
  }
}

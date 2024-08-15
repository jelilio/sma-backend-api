package io.github.jelilio.smbackend.common.dto.response;

import java.time.Instant;

public record UserCommunityRes(
    String id,
    String memberId,
    String name,
    String email,
    String avatarUrl,
    String avatarType,
    String username,
    Instant membershipDate,
    Instant createdDate,
    boolean youFollowing,
    boolean itsFollowing,
    boolean itsMember,
    boolean itsPending,
    Long memberCount,

    String ownerId,
    String ownerName,
    String ownerUsername
) {
}
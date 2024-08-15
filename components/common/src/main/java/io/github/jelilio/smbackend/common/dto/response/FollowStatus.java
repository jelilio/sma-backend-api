package io.github.jelilio.smbackend.common.dto.response;

public record FollowStatus(
    Boolean itsFollowing,
    Boolean youFollowing
) {
}

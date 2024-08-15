package io.github.jelilio.smbackend.common.dto.response;

public record FollowStatusCount(
    Long followings,
    Long followers,
    Long communities,
    Long clubs
) {
}

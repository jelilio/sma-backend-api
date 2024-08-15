package io.github.jelilio.smbackend.common.dto.response;

public record MemberStatusCount(
    Long pendings,
    Long members
) {
}

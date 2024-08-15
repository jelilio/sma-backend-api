package io.github.jelilio.smbackend.common.dto.response;

public record MemberStatus(
    Boolean itsMember,
    Boolean itsPending
) {
}

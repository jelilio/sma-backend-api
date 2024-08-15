package io.github.jelilio.smbackend.common.dto.response;

public record OwnerRes(
    String ownerId,
    String ownerName,
    String ownerUsername,

    String requesterId,
    String requesterName,
    String requesterUsername
) {
}

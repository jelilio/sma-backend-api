package io.github.jelilio.smbackend.commonutil.dto.response;

import java.time.Instant;

public record PostProRes (
    String id,
    String caption,
    String imageUrl,
    String imageType,
    Instant createdDate,
    PostOwner owner,
    PostCount count,
    PostAction myAction
) {
}

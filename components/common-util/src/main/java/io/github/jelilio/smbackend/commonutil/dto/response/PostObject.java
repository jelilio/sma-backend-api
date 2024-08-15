package io.github.jelilio.smbackend.commonutil.dto.response;

import java.util.UUID;

public record PostObject (
    UUID id,
    String caption,
    String imageUrl,
    UUID ownerId
) {

}

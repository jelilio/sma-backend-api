package io.github.jelilio.smbackend.commonutil.dto.response;

import io.github.jelilio.smbackend.commonutil.entity.enumeration.UserType;

import java.time.Instant;

public record PostOwner(
    String id,
    String name,
    String username,
    UserType userType,
    String avatarUrl,
    Instant verifiedDate,
    boolean verified
) {

}

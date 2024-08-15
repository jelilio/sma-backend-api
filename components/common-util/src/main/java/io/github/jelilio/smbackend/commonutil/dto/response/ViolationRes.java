package io.github.jelilio.smbackend.commonutil.dto.response;

import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Severity;

import java.time.Instant;

public record ViolationRes(
    String id,
    String caption,
    String imageUrl,
    String category,
    Action action,
    Severity severity,
    UserRes owner,
    PostRes post,
    Instant createdDate
) {

}

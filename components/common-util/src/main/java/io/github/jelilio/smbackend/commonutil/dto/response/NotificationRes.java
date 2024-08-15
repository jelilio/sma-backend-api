package io.github.jelilio.smbackend.commonutil.dto.response;

import io.github.jelilio.smbackend.commonutil.entity.enumeration.NotificationType;

import java.time.Instant;

public record NotificationRes(
    String id,
    String caption,
    NotificationType type,
    UserRes owner,
    UserRes initiator,
    Instant createdAt
) {
}

package io.github.jelilio.smbackend.common.dto.response;

import java.time.Instant;

public record ClubRequestRes(
    String id,
    String name,
    String purpose,
    UserRes requester,
    UserRes community,
    Instant approvalDate
) {
  public String getOwnerId() {
    if(community == null) return null;

    return community.id();
  }

  public String getOwnerName() {
    if(community == null) return null;

    return community.name();
  }

  public String getOwnerUsername() {
    if(community == null) return null;

    return community.username();
  }

  public String getRequesterId() {
    if(requester == null) return null;

    return requester.id();
  }

  public String getRequesterName() {
    if(requester == null) return null;

    return requester.name();
  }

  public String getRequesterUsername() {
    if(requester == null) return null;

    return requester.username();
  }
}

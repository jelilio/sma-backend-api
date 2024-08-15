package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.quarkus.hibernate.reactive.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.UUID;

@RegisterForReflection
public class Club extends FollowPro {
  public UUID memberId;
  public Instant membershipDate;
  public Instant createdDate;
  public Long memberCount;
  public boolean itsMember;
  public boolean itsPending;

  public UUID ownerId; // staff or community
  public String ownerName; // staff or community
  public String ownerUsername; // staff or community

  public UUID requesterId;
  public String requesterName;
  public String requesterUsername;

  public User requester;
  public User community;

  public Club(
      UUID id, UUID memberId, String name, String email, String username, String avatarUrl, String avatarType,
      UserType userType, Instant verifiedDate, Instant createdDate,
      UUID ownerId, String ownerName, String ownerUsername, UUID requesterId, String requesterName, String requesterUsername,
      Long memberCount, boolean itsMember, boolean itsPending) {
    this.id = id;
    this.memberId = memberId;
    this.name = name;
    this.email = email;
    this.username = username;
    this.avatarUrl = avatarUrl;
    this.avatarType = avatarType;
    this.userType = userType;
    this.memberCount = memberCount;
    this.verifiedDate = verifiedDate;
    this.createdDate = createdDate;

    this.ownerId = ownerId;
    this.ownerName = ownerName;
    this.ownerUsername = ownerUsername;

    this.requesterId = requesterId;
    this.requesterName = requesterName;
    this.requesterUsername = requesterUsername;

    this.itsMember = itsMember;
    this.itsPending = itsPending;

    this.requester = new User(requesterId, requesterName, requesterUsername);
    this.community = new User(requesterId, ownerName, ownerUsername);
  }

  public Club(
      @ProjectedFieldName("otherUser.id") UUID id,
      @ProjectedFieldName("otherUser.name") String name,
      @ProjectedFieldName("otherUser.email") String email,
      @ProjectedFieldName("member") Instant membershipDate,
      @ProjectedFieldName("createdDate") Instant followingDate
  ) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.membershipDate = membershipDate;
    this.followingDate = followingDate;
  }
}

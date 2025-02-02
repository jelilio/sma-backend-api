package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.quarkus.hibernate.reactive.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.UUID;

@RegisterForReflection
public class UserCommunity extends FollowPro {
  public UUID memberId;
  public Instant membershipDate;
  public Instant createdDate;
  public Long memberCount;
  public boolean youFollowing;
  public boolean itsFollowing;
  public boolean itsMember;
  public boolean itsPending;

//  public UUID ownerId;
//  public String ownerName;
//  public String ownerUsername;

  public UserCommunity(
      UUID id, UUID memberId, String name, String email, String username, String avatarUrl, String avatarType,
      UserType userType, Instant verifiedDate, Instant createdDate,
      Long memberCount, boolean youFollowing, boolean itsFollowing, boolean itsMember, boolean itsPending) {
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

//    this.ownerId = ownerId;
//    this.ownerName = ownerName;
//    this.ownerUsername = ownerUsername;

    this.youFollowing = youFollowing;
    this.itsFollowing = itsFollowing;

    this.itsMember = itsMember;
    this.itsPending = itsPending;
  }

  public UserCommunity(
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

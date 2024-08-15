package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.Instant;

public class PostRes {
  public String id;
  public String caption;
  public String imageUrl;
  public String imageType;
  public UserType userType;
  public String avatarUrl;
  public String ownerId;
  public String owner;
  public String ownerUsername;
  public String originalPostId;
  public String originalPostOwnerId;
  public String originalPostOwnerName;
  public String originalPostOwnerUsername;
  public UserType originalPostOwnerUserType;
  public String originalPostOwnerAvatarUrl;
  public boolean share;
  public Instant createdDate;
  public Long shareCount;
  public Long originalShareCount;;
  public boolean itsShare;
  public boolean itsOriginalShare;

  public Instant ownerVerifiedDate;
  public Instant originalPostOwnerVerifiedDate;

  public boolean ownerVerified;
  public boolean originalPostOwnerVerified;
}

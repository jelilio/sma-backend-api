package io.github.jelilio.smbackend.botmanager.dto.response;

import io.github.jelilio.smbackend.botmanager.entity.enumeration.UserType;

import java.time.Instant;

public class PostRes {
  public String id;
  public String caption;
  public String imageUrl;
  public String imageType;
  public UserType userType;
  public String ownerId;
  public String owner;
  public String ownerUsername;
  public String originalPostId;
  public String originalPostOwnerId;
  public String originalPostOwnerName;
  public String originalPostOwnerUsername;
  public boolean share;
  public Instant createdDate;
}

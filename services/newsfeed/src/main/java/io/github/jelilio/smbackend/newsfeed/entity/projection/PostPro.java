package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.Instant;
import java.util.UUID;

public class PostPro {
  public UUID id;
  public String caption;
  public String imageUrl;
  public String imageType;
  public Instant createdDate;
  public User owner;
  public Count count;
  public Action myAction;

  public PostPro(
      UUID id, String caption, String imageUrl, String imageType, Instant createdDate,
      UUID ownerId, String ownerName, String ownerUsername, UserType userType, String avatarUrl,
      Instant ownerVerifiedDate, Long sharedCount, Long likedCount, Long repliedCount,
      Boolean iShared, Boolean iLiked, Boolean iReplied
  ) {
    this.id = id;
    this.caption = caption;
    this.imageUrl = imageUrl;
    this.imageType = imageType;
    this.createdDate = createdDate;

    this.owner = new User(ownerId, ownerName, ownerUsername, userType, avatarUrl, ownerVerifiedDate);
    this.count = new Count(likedCount, sharedCount, repliedCount);
    this.myAction = new Action(iLiked, iShared, iReplied);
  }

  public record User (
      UUID id,
      String name,
      String username,
      UserType userType,
      String avatarUrl,
      Instant verifiedDate
  ) {
    public boolean isVerified() {
      return verifiedDate != null;
    }
  }

  public record Action(
      Boolean liked,
      Boolean shared,
      Boolean replied
  ) {
  }

  public record Count(
      Long likes,
      Long shares,
      Long replies
  ) {
  }
}

package io.github.jelilio.smbackend.newsfeed.entity.projection;

import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.quarkus.hibernate.reactive.panache.common.ProjectedFieldName;

import java.time.Instant;
import java.util.UUID;

public class PostReal {
  public UUID id;
  public String caption;
  public UUID ownerId;
  public String owner;
  public String ownerUsername;
  public UserType userType;
  public String avatarUrl;
  public Instant ownerVerifiedDate;
  public UUID originalPostId;
  public UUID originalPostOwnerId;
  public String originalPostOwnerName;
  public String originalPostOwnerUsername;
  public UserType originalPostOwnerUserType;
  public String originalPostOwnerAvatarUrl;
  public Instant originalPostOwnerVerifiedDate;
  public boolean share;
  public Instant createdDate;
  public String imageUrl;
  public String imageType;
  public Long shareCount;
  public Long originalShareCount;
  public boolean itsShare;
  public boolean itsOriginalShare;

  public boolean ownerVerified;
  public boolean originalPostOwnerVerified;

  public PostReal(
      @ProjectedFieldName("id") UUID id,
      @ProjectedFieldName("caption") String caption,
      @ProjectedFieldName("imageUrl") String imageUrl,
      @ProjectedFieldName("imageType") String imageType,
      @ProjectedFieldName("owner.id") UUID ownerId,
      @ProjectedFieldName("owner.name") String owner,
      @ProjectedFieldName("owner.username") String ownerUsername,
      @ProjectedFieldName("owner.userType") UserType ownerUserType,
      @ProjectedFieldName("owner.avatarUrl") String ownerAvatarUrl,
      @ProjectedFieldName("owner.verifiedDate") Instant ownerVerifiedDate,
      @ProjectedFieldName("originalPost.id") UUID originalPostId,
      @ProjectedFieldName("originalPost.owner.id") UUID originalPostOwnerId,
      @ProjectedFieldName("originalPost.owner.name") String originalPostOwnerName,
      @ProjectedFieldName("originalPost.owner.username") String originalPostOwnerUsername,
      @ProjectedFieldName("originalPost.owner.userType") UserType originalPostOwnerUserType,
      @ProjectedFieldName("originalPost.owner.avatarUrl") String originalPostOwnerImageUrl,
      @ProjectedFieldName("originalPost.owner.verifiedDate") Instant originalPostOwnerVerifiedDate,
      @ProjectedFieldName("createdDate") Instant createdDate,
      @ProjectedFieldName("shareCount") Long shareCount,
      @ProjectedFieldName("shareUserCount") Long shareUserCount,
      @ProjectedFieldName("originalShareCount") Long originalShareCount,
      @ProjectedFieldName("originalShareUserCount") Long originalShareUserCount
  ) {
    this.id = id;
    this.caption = caption;
    this.ownerId = ownerId;
    this.owner = owner;
    this.ownerUsername = ownerUsername;
    this.createdDate = createdDate;
    this.imageUrl = imageUrl;
    this.imageType = imageType;
    this.userType = ownerUserType;
    this.avatarUrl = ownerAvatarUrl;
    this.ownerVerifiedDate = ownerVerifiedDate;

    if(originalPostId != null) {
      this.originalPostId = originalPostId;
      this.originalPostOwnerId = originalPostOwnerId;
      this.originalPostOwnerName = originalPostOwnerName;
      this.originalPostOwnerUsername = originalPostOwnerUsername;
      this.originalPostOwnerUserType = originalPostOwnerUserType;
      this.originalPostOwnerAvatarUrl = originalPostOwnerImageUrl;
      this.originalPostOwnerVerifiedDate = originalPostOwnerVerifiedDate;

      this.originalPostOwnerVerified = originalPostOwnerVerifiedDate != null;
    }

    this.shareCount = shareCount;
    this.originalShareCount = originalShareCount;
    this.share = shareCount > 0 || originalShareCount > 0;

    this.itsShare = shareUserCount > 0;
    this.itsOriginalShare = originalShareUserCount > 0;

    this.ownerVerified = ownerVerifiedDate != null;
  }

  @Override
  public String toString() {
    return "PostReal{" +
        "id=" + id +
        ", caption='" + caption + '\'' +
        ", owner='" + owner + '\'' +
        ", originalPostId=" + originalPostId +
        ", originalPostOwnerName='" + originalPostOwnerName + '\'' +
        ", share=" + share +
        '}';
  }
}

package io.github.jelilio.smbackend.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractSoftDeletableEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.newsfeed.entity.key.UserPostId;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostPro;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "user_posts")
@EntityListeners(AuditingEntityListener.class)
public class UserPost extends AbstractSoftDeletableEntity {
  private static final String notDeletedEntries = " and p1_0.deletedAt IS NULL ";

  public static final String postSelectedFields =
      " DISTINCT p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, o1_0.id, o1_0.name, o1_0.username, o1_0.type, " +
          "o1_0.avatarUrl, o1_0.verifiedDate, p1_0.post.originalPost.id, o3_0.id, o3_0.name, o3_0.username, o3_0.type, o3_0.avatarUrl, o3_0.verifiedDate, " +
          "p1_0.post.createdDate ";
  public static final String postSelectedFieldsGroupBy =
      " p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, o1_0.id, o1_0.name, o1_0.username, o1_0.type, " +
          "o1_0.avatarUrl, o1_0.verifiedDate, p1_0.post.originalPost.id, o3_0.id, o3_0.name, o3_0.username, o3_0.type, o3_0.avatarUrl, o3_0.verifiedDate, " +
          "p1_0.post.createdDate ";
  public static final String aggregatorFields =
      ", COUNT(*) FILTER (WHERE s1_0.id.postId = p1_0.id and s1_0.liked is false) AS shareCount " +
          ", COUNT(*) FILTER (WHERE s1_0.id.userId = o1_0.id and s1_0.liked is false) AS shareUserCount " +
          ", COUNT(*) FILTER (WHERE s2_0.id.postId = p1_0.originalPost.id and s2_0.liked is false) AS originalShareCount " +
          ", COUNT(*) FILTER (WHERE s2_0.id.userId = o3_0.id and s2_0.liked is false) AS originalShareUserCount ";
  public static final String aggregatorFieldsWithLoggedInId =
      ", COUNT(*) FILTER (WHERE s1_0.id.postId = p1_0.id and s1_0.liked is false) AS shareCount " +
          ", COUNT(*) FILTER (WHERE s1_0.id.userId = ?1 and s1_0.liked is false) AS shareUserCount " +
          ", COUNT(*) FILTER (WHERE s2_0.id.postId = p1_0.originalPost.id and s2_0.liked is false) AS originalShareCount " +
          ", COUNT(*) FILTER (WHERE s2_0.id.userId = ?1 and s2_0.liked is false) AS originalShareUserCount ";
  public static final String fromTables =
      "    from UserPost p1_0 " +
          "    join User o1_0 on o1_0.id=p1_0.owner.id " +
          "    outer join Share s1_0 on s1_0.id.postId=p1_0.id " +
          "    outer join Share s2_0 on s2_0.id.postId=p1_0.originalPost.id " +
          "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
          "    outer join User o3_0 on o3_0.id=o2_0.owner.id ";

  @Id
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UserPostId id;

  @ManyToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  public User user;

  @ManyToOne
  @JoinColumn(name = "post_id", insertable = false, updatable = false)
  public Post post;

  @Column(name = "liked_date")
  public Instant likedDate;

  @Column(name = "shared_date")
  public Instant sharedDate;

  @Column(name = "recipient_date")
  public Instant recipientDate;

  @Column(name = "reply_parent_id")
  public UUID replyParentId;

  public UserPost() {}

  public UserPost(User user, Post post) {
    super();
    this.id = new UserPostId(user.id, post.id);
  }

  public UserPost(User user, Post post, Post parentPost) {
    super();
    this.id = new UserPostId(user.id, post.id);
    this.replyParentId = parentPost.id;
  }

  public static Uni<UserPost> findById(String userId, String postId) {
    return findById(new UserPostId(UUID.fromString(userId), UUID.fromString(postId)));
  }

  public static Uni<UserPost> findByIdAndShared(String userId, String postId) {
    return find("id.userId = ?1 and id.postId = ?2 and sharedDate is not null", UUID.fromString(userId), UUID.fromString(postId)).firstResult();
  }

  public static Uni<UserPost> findByIdAndLiked(String userId, String postId) {
    return find("id.userId = ?1 and id.postId = ?2 and likedDate is not null", UUID.fromString(userId), UUID.fromString(postId)).firstResult();
  }

  public static Uni<Long> countById(UserPostId id) {
    return UserPost.count("id = ?1", id);
  }

  public static Uni<Long> alreadyShared(UserPostId id) {
    return UserPost.count("id = ?1 and sharedDate is not null", id);
  }

  public static Uni<Long> alreadyLiked(UserPostId id) {
    return UserPost.count("id = ?1 and likedDate is not null", id);
  }

  private static Uni<UserPost> findById(UserPostId id) {
    return UserPost.find("id = ?1", id).firstResult();
  }

  public static Uni<Long> countAllPost() {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE t.post.recipient is null and t.deletedAt IS NULL");
  }

  public static Uni<Long> countAllPost(String query) {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE lower(t.post.caption) like ?1 and t.post.recipient is null and t.deletedAt IS NULL", query);
  }

  public static Uni<Long> countFollowingPost(Set<UUID> followings) {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE t.id.userId in ?1 and t.deletedAt IS NULL", followings);
  }

  public static Uni<Long> countMyPost(User loggedInUser) {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE t.user = ?1 and t.deletedAt IS NULL", loggedInUser);
  }

  public static Uni<Long> countOtherPost(User otherUser) {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE t.user = ?1 and t.deletedAt IS NULL", otherUser);
  }

  public static Uni<Long> countOtherNonMemberPost(User otherUser) {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE t.user = ?1 and t.post.recipient is null and t.deletedAt IS NULL", otherUser);
  }

  public static Uni<Long> countOtherMemberPost(User otherUser) {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE (t.user = ?1 or t.post.recipient = ?1) and t.deletedAt IS NULL", otherUser);
  }

  public static Uni<Long> countPostReply(UUID replyParentId) {
    return UserPost.count("SELECT count(distinct t.id.postId) FROM UserPost t WHERE t.replyParentId = ?1 and t.deletedAt IS NULL", replyParentId);
  }

  public static Uni<Long> delete(String id, User user) {
    return UserPost.update("deletedAt = ?1 WHERE id = ?2 and user = ?3", Instant.now(), new UserPostId(user.id, UUID.fromString(id)), user)
        .map(Long::valueOf);
  }

  public static Uni<Long> deleteByPost(String postId, User user) {
    return UserPost.update("deletedAt = ?1 WHERE post.id = ?2 and user = ?3", Instant.now(), UUID.fromString(postId), user)
        .map(Long::valueOf);
  }

  public static UserPost share(User user, Post post) {
    var userPost =  new UserPost(user, post);
    userPost.sharedDate = Instant.now();
    return userPost;
  }

  public static UserPost like(User user, Post post) {
    var userPost =  new UserPost(user, post);
    userPost.likedDate = Instant.now();
    return userPost;
  }

  // all my post, including shared, liked
  public static PanacheQuery<PostPro> findMyPost(User loggedInUser, int size, int index) {
    return Post.find(
        "SELECT " +
            "   DISTINCT p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
            "   o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate,  p1_0.createdDate, " +
            "   COUNT(*) FILTER (WHERE s1_0.sharedDate != null) AS sharedCount " +
            "   FROM UserPost p1_0 " +
            "   JOIN User o1_0 ON o1_0.id = p1_0.post.owner.id " +
            "   OUTER JOIN UserPost s1_0 ON s1_0.id.userId = p1_0.post.owner.id" +
            "   WHERE p1_0.id.userId = ?1 and p1_0.deletedAt IS NULL " +
            "   GROUP BY " +
            "   p1_0.id.postId, p1_0.post.caption, p1_0.post.imageUrl, p1_0.post.imageType, p1_0.post.createdDate, " +
            "   o1_0.id, o1_0.name, o1_0.username, o1_0.type, o1_0.avatarUrl, o1_0.verifiedDate,  p1_0.createdDate " +
            "   ORDER BY p1_0.createdDate DESC " +
            "   LIMIT ?2 OFFSET ?3",
        loggedInUser.id, size, index
    ).project(PostPro.class);
  }

  public boolean isSharedOnly() {
    if(likedDate != null || replyParentId != null) {
      return false;
    }

    return true;
  }

  public boolean isLikedOnly() {
    if(sharedDate != null || replyParentId != null) {
      return false;
    }

    return true;
  }

  public UserPost share() {
    this.sharedDate = Instant.now();
    return this;
  }

  public UserPost like() {
    this.likedDate = Instant.now();
    return this;
  }
}

package io.github.jelilio.smbackend.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractSoftDeletableEntity;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.newsfeed.entity.projection.PostReal;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE posts SET deleted_at = now() WHERE id = $1", check = ResultCheckStyle.COUNT)
@FilterDefs(value = {
    @FilterDef(name = "nonDeletedEntries", defaultCondition = "deletedAt IS NULL"),
    @FilterDef(name = "deletedEntries", defaultCondition = "deleted_at IS NOT NULL"),
    @FilterDef(name = "allEntries", defaultCondition = "deleted_at IS NULL or deleted_at IS NOT NULL")
})
public class Post extends AbstractSoftDeletableEntity {
  private static final String notDeletedEntries = " and p1_0.deletedAt IS NULL ";

  public static final String postSelectedFields =
      " p1_0.id, p1_0.caption, p1_0.imageUrl, p1_0.imageType, o1_0.id, o1_0.name, o1_0.username, o1_0.type, " +
          "o1_0.avatarUrl, o1_0.verifiedDate, p1_0.originalPost.id, o3_0.id, o3_0.name, o3_0.username, o3_0.type, o3_0.avatarUrl, o3_0.verifiedDate, " +
          "p1_0.createdDate ";
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
          "    from Post p1_0 " +
          "    join User o1_0 on o1_0.id=p1_0.owner.id " +
          "    outer join Share s1_0 on s1_0.id.postId=p1_0.id " +
          "    outer join Share s2_0 on s2_0.id.postId=p1_0.originalPost.id " +
          "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
          "    outer join User o3_0 on o3_0.id=o2_0.owner.id ";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;

  @Size(max = 150, min = 5)
  @Column(name = "caption", length = 150)
  public String caption;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id")
  public User owner;

  @OneToOne
  @JoinColumn(name = "original_post_id")
  public Post originalPost;

  // for comment/reply
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_post_id")
  public Post parentPost;

  @Column(name = "shared_date")
  public Instant sharedDate;

  @Column(name = "liked_date")
  public Instant likedDate;

  @Column(name = "shared_count")
  public long sharedCount;

  @Column(name = "liked_count")
  public long likedCount;

  @Column(name = "image_url")
  public String imageUrl;

  @Column(name = "image_type")
  public String imageType;

  // for community/club
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "recipient_id")
  public User recipient;

  public static Uni<Post> findById(String id) {
    return Post.find("id = ?1 and deletedAt IS NULL", UUID.fromString(id))
        .firstResult();
//    return Post.findById(UUID.fromString(id));
  }

  public static Uni<Boolean> deleteById(String id) {
    return Post.deleteById(UUID.fromString(id));
  }

  public static Uni<Long> delete(String id, User user) {
    return Post.update("deletedAt = ?1 WHERE id = ?2 and owner = ?3", Instant.now(), UUID.fromString(id), user)
        .map(Long::valueOf);
//    return Post.delete("id = ?1 and owner = ?2", UUID.fromString(id), user);
  }

  public static Uni<Long> isShared(User user, Post post) {
    return Post.find("owner = ?1 and originalPost = ?2 and sharedDate is not null", user, post).count();
  }

  @Deprecated
  public static PanacheQuery<Post> ownerPosts(User user) {
    return Post
        .find("select p from Post p left join Share s on p.owner = s.sharedBy where p.owner = ?1" + notDeletedEntries, user);
//        .project(Post.class);
  }

  public static PanacheQuery<Post> find(User user) {
    return Post.find("owner = ?1", user);
  }

  public static PanacheQuery<Post> find(Set<User> users) {
    return Post.findAll();
  }

  @Deprecated
  public static PanacheQuery<Post> find_(Set<User> users) {
    return Post.find(
        "select " +
            postSelectedFields +
            "    from Post p1_0 " +
            "    join User o1_0 on o1_0.id=p1_0.owner.id " +
            "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
            "    outer join User o3_0 on o3_0.id=o2_0.owner.id " +
            "    where p1_0.owner in ?1  " + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc",
        users
    );
  }

  // pending modification
  public static Uni<PostReal> findAllPostsPost(UUID loggedInUserId, String id) {
    return Post.find(
        "select " +
            postSelectedFields +
            aggregatorFieldsWithLoggedInId +
            fromTables +
            "    where p1_0.id = ?2  " + notDeletedEntries +   // and p1_0.recipient is null+
            "    group by " +
            postSelectedFields,
        loggedInUserId, UUID.fromString(id)
    ).project(PostReal.class).firstResult();
  }

  public static PanacheQuery<Post> findAllPosts(Set<User> users, int size, int index) {
    return Post.find(
        "select " +
            postSelectedFields +
            aggregatorFields +
            fromTables +
            "    where p1_0.owner in ?1 and p1_0.recipient is null " + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    limit ?2 offset ?3",
         users, size, index
    );
  }

  public static PanacheQuery<Post> findAllPosts(String userId, Set<User> users, int size, int index) {
    return Post.find(
        "select " +
            postSelectedFields +
            aggregatorFieldsWithLoggedInId +
            fromTables +
            "    where p1_0.owner in ?2 and p1_0.recipient is null " + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    limit ?3 offset ?4",
         UUID.fromString(userId), users, size, index
    );
  }

  public static PanacheQuery<Post> findAllPostReplies(UUID userId, UUID postId, int size, int index) {
    return Post.find(
        "select " +
            postSelectedFields +
            aggregatorFieldsWithLoggedInId +
            fromTables +
            "    where p1_0.parentPost.id = ?2 " + notDeletedEntries +  // and p1_0.recipient is null
    "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    limit ?3 offset ?4",
        userId, postId, size, index
    );
  }

  public static Uni<Long> countAllPosts(Set<User> users) {
    return Post.count(
            "    from Post p1_0 " +
            "    join User o1_0 on o1_0.id=p1_0.owner.id " +
            "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
            "    outer join User o3_0 on o3_0.id=o2_0.owner.id " +
            "    where p1_0.owner in ?1 and p1_0.recipient is null "  + notDeletedEntries , users
    );
  }

  public static Uni<Long> countAllPostReplies(UUID postId) {
    return Post.count(
            "    from Post p1_0 " +
            "    join User o1_0 on o1_0.id=p1_0.owner.id " +
            "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
            "    outer join User o3_0 on o3_0.id=o2_0.owner.id " +
            "    where p1_0.parentPost.id = ?1" + notDeletedEntries, postId
    );
  }


  public static Uni<Long> countAllPosts() {
    return Post.count(
            "    from Post p1_0 " +
            "    join User o1_0 on o1_0.id=p1_0.owner.id " +
            "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
            "    outer join User o3_0 on o3_0.id=o2_0.owner.id " +
            "    where p1_0.recipient is null  " + notDeletedEntries
    );
  }

  public static PanacheQuery<Post> findAllPosts(int size, int index) {
    return Post.find(
        "SELECT " +
            postSelectedFields +
            aggregatorFields +
            fromTables +
            "    where p1_0.recipient is null  "  + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    LIMIT ?1 OFFSET ?2", size, index
    );
  }

  public static PanacheQuery<Post> findAllPosts(String userId, int size, int index) {
    return Post.find(
        "SELECT " +
            postSelectedFields +
            aggregatorFieldsWithLoggedInId +
            fromTables +
            "    where p1_0.recipient is null  "  + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    LIMIT ?2 OFFSET ?3", UUID.fromString(userId), size, index
    );
  }

  public static Uni<Long> countAllCommunityPosts(Set<User> users) {
    return Post.count(
        "    from Post p1_0 " +
            "    join User o1_0 on o1_0.id=p1_0.owner.id " +
            "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
            "    outer join User o3_0 on o3_0.id=o2_0.owner.id " +
            "    where p1_0.owner in ?1 or p1_0.recipient in ?1 " + notDeletedEntries, users
    );
  }

  public static PanacheQuery<Post> findAllCommunityPosts(Set<User> communitiesOrClubs, int size, int index) {
    return Post.find(
        "SELECT " +
            postSelectedFields +
            aggregatorFields +
            fromTables +
            "    where p1_0.owner in ?1 or p1_0.recipient in ?1  "  + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    LIMIT ?2 OFFSET ?3", communitiesOrClubs, size, index
    );
  }

  public static PanacheQuery<Post> findAllCommunityPosts(String userId, Set<User> communitiesOrClubs, int size, int index) {
    return Post.find(
        "SELECT " +
            postSelectedFields +
            aggregatorFieldsWithLoggedInId +
            fromTables +
            "    where p1_0.owner in ?2 or p1_0.recipient in ?2  " + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    LIMIT ?3 OFFSET ?4", UUID.fromString(userId), communitiesOrClubs, size, index
    );
  }

  public static Uni<Long> countMyPostsAsCommunity(Set<User> users) {
    return Post.count(
        "    from Post p1_0 " +
            "    join User o1_0 on o1_0.id=p1_0.owner.id " +
            "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
            "    outer join User o3_0 on o3_0.id=o2_0.owner.id " +
            "    where p1_0.owner in ?1 " + notDeletedEntries, users
    );
  }

  public static PanacheQuery<Post> findMyPostsAsCommunity(Set<User> communitiesOrClubs, int size, int index) {
    return Post.find(
        "SELECT " +
            postSelectedFields +
            aggregatorFields +
            fromTables +
            "    where p1_0.owner in ?1  " + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    LIMIT ?2 OFFSET ?3", communitiesOrClubs, size, index
    );
  }

  public static PanacheQuery<Post> findMyPostsAsCommunity(String userId, Set<User> communitiesOrClubs, int size, int index) {
    return Post.find(
        "SELECT " +
            postSelectedFields +
            aggregatorFieldsWithLoggedInId +
            fromTables +
            "    where p1_0.owner in ?2  " + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    LIMIT ?3 OFFSET ?4", UUID.fromString(userId), communitiesOrClubs, size, index
    );
  }

  public static Uni<Long> countCommunityPosts(Set<User> users) {
    return Post.count(
        "    from Post p1_0 " +
            "    join User o1_0 on o1_0.id=p1_0.owner.id " +
            "    outer join Post o2_0 on o2_0.id=p1_0.originalPost.id" +
            "    outer join User o3_0 on o3_0.id=o2_0.owner.id " +
            "    where p1_0.recipient in ?1 " + notDeletedEntries, users
    );
  }

  public static PanacheQuery<Post> findCommunityPosts(Set<User> communitiesOrClubs, int size, int index) {
    return Post.find(
        "SELECT " +
            postSelectedFields +
            aggregatorFields +
            fromTables +
            "    where p1_0.recipient in ?1  "  + notDeletedEntries +
            "    group by " +
            postSelectedFields +
            "    order by p1_0.createdDate desc " +
            "    LIMIT ?2 OFFSET ?3", communitiesOrClubs, size, index
    );
  }

  public Post() {}

  public Post(String caption, User owner) {
    this.caption = caption;
    this.owner = owner;
  }

  public Post(String caption, Post parentPost, User owner) {
    this.caption = caption;
    this.owner = owner;

    this.parentPost = parentPost;
  }

  public Post(Post oldPost, User owner) {
    this.caption = oldPost.caption;
    this.recipient = oldPost.recipient;
    this.imageType = oldPost.imageType;
    this.imageUrl = oldPost.imageUrl;
    this.originalPost = oldPost;
    this.sharedDate = Instant.now();
    this.owner = owner;
  }
}

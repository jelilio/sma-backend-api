package io.github.jelilio.smbackend.newsfeed.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.github.jelilio.smbackend.newsfeed.entity.key.FollowId;
import io.github.jelilio.smbackend.newsfeed.entity.projection.*;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Cacheable
@Table(name = "follow")
@EntityListeners(AuditingEntityListener.class)
public class Follow extends AbstractAuditingEntity {
  private static final Logger logger = LoggerFactory.getLogger(Follow.class);

  @Id
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public FollowId id;

  @ManyToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  public User user;

  @ManyToOne
  @JoinColumn(name = "other_user_id", insertable = false, updatable = false)
  public User otherUser;

  @Column(name = "member")
  public Instant member;

  @Column(name = "member_request")
  public Instant memberRequest;

  @Column(name = "organizer")
  public Instant organizer;

  public Follow() {}

  public Follow(User user, User otherUser) {
    super();
    this.id = new FollowId(user.id, otherUser.id);
  }

  public static PanacheQuery<Follow> findFollowings(String userId) {
    return Follow.find("id.userId = ?1 and member is not null and memberRequest is null", UUID.fromString(userId));
  }

  public static PanacheQuery<Follow> findFollowingCommunities(String userId) {
    return Follow.find("id.userId = ?1 and member is not null and memberRequest is not null", UUID.fromString(userId));
  }

  public static PanacheQuery<Following> findFollowings_(String userId) {
    return Follow.find("id.userId = ?1 and member is not null and memberRequest is null", UUID.fromString(userId)).project(Following.class);
  }

  public static Uni<Long> countFollowings_(String userId) {
    return Follow.count("id.userId = ?1 and member is not null and memberRequest is null", UUID.fromString(userId));
  }

  public static Uni<Long> countFollowingsCommunitiesClubs_(String userId) {
    return Follow.count("id.userId = ?1 and member is not null and memberRequest is not null", UUID.fromString(userId));
  }

  public static Uni<Long> countFollowingsCommunitiesOrClubs_(String userId, UserType userType) {
    return Follow.count("id.userId = ?1 and otherUser.type = ?2 and member is not null and memberRequest is not null", UUID.fromString(userId), userType);
  }

  public static PanacheQuery<Follow> findFollowers(String userId) {
    return Follow.find("id.otherUserId = ?1 and member is not null and memberRequest is null", UUID.fromString(userId));
  }

  public static PanacheQuery<Follow> findMembers(String userId) {
    return Follow.find("id.otherUserId = ?1 and member is not null and memberRequest is not null", UUID.fromString(userId));
  }

  public static PanacheQuery<Follower> findFollowers_(String userId) {
    return Follow.find("id.otherUserId = ?1 and member is not null and memberRequest is null", UUID.fromString(userId)).project(Follower.class);
  }

  // this
  public static Uni<Long> countFollowers_(String userId) {
    return Follow.count("id.otherUserId = ?1 and member is not null and memberRequest is null", UUID.fromString(userId));
  }

  public static PanacheQuery<Member> findMembers_(String communityId) {
    return Follow.find("id.otherUserId = ?1 AND member is not null and memberRequest is not null", UUID.fromString(communityId)).project(Member.class);
  }

  public static Uni<Long> countPendings_(String userId) {
    return Follow.count("id.otherUserId = ?1 and member is null and memberRequest is not null", UUID.fromString(userId));
  }

  public static Uni<Long> countMembers_(String userId) {
    return Follow.count("id.otherUserId = ?1 and member is not null and memberRequest is not null", UUID.fromString(userId));
  }

  public static PanacheQuery<Member> findMemberRequests_(String communityId) {
    return Follow.find("id.otherUserId = ?1 and memberRequest is not null", UUID.fromString(communityId)).project(Member.class);
  }

//  public static PanacheQuery<Community> findCommunities_(String memberId) {
//    return Follow.find("id.userId = ?1", UUID.fromString(memberId)).project(Community.class);
//  }

  public static Uni<Long> countById(FollowId id) {
    return Follow.count("id = ?1", id);
  }

  public static Uni<Long> countByAlreadyAMember(FollowId id) {
    // return Follow.count("id = ?1 and member is not null and memberRequest is not null", id);
    return Follow.count("id.otherUserId = ?1 and id.userId = ?2 and member is not null and memberRequest is not null", id.otherUserId, id.userId);
  }

  public static Uni<Long> countByAlreadySentARequest(FollowId id) {
    return Follow.count("id = ?1 and memberRequest is not null", id);
  }

  public static Uni<FollowCount> countFollowByUserId(User user) {
    logger.info("countFollowByUserId: id: {}", user.id);

    return Follow.find(
        "SELECT " +
            "COUNT(*) FILTER (WHERE user = ?1 AND member is not null AND memberRequest is null) AS followings, " +
            "COUNT(*) FILTER (WHERE user = ?1 AND member is not null AND otherUser.type = 'COMMUNITY' AND memberRequest is not null) AS communities, " +
            "COUNT(*) FILTER (WHERE user = ?1 AND member is not null AND otherUser.type = 'CLUB' AND memberRequest is not null) AS clubs, " +
            "COUNT(*) FILTER (WHERE otherUser = ?1 AND member is not null AND memberRequest is null) AS followers " +
            "FROM Follow", user).project(FollowCount.class).firstResult();
  }

  public static Uni<FollowOnlyCount> checkFollowById(FollowId id) {
    return Follow.find(
        "SELECT " +
            "COUNT(*) FILTER (WHERE otherUser.id = ?1 AND user.id = ?2 AND member is not null AND memberRequest is null) AS followings, " +
            "COUNT(*) FILTER (WHERE user.id = ?1 AND otherUser.id = ?2 AND member is not null AND memberRequest is null) AS followers " +
            "FROM Follow", id.userId, id.otherUserId)
        .project(FollowOnlyCount.class).firstResult();
  }

  public static Uni<MemberCount> countMemberByUserId(User user) {
    return Follow.find(
        "SELECT " +
            "COUNT(*) FILTER (WHERE otherUser = ?1 AND member is not null AND memberRequest is not null) AS members, " +
            "COUNT(*) FILTER (WHERE otherUser = ?1 AND member is null AND memberRequest is not null) AS pendings " +
            "FROM Follow", user).project(MemberCount.class).firstResult();
  }

  public static Uni<MemberCount> checkMemberById(FollowId id) {
    return Follow.find(
            "SELECT " +
                "COUNT(*) FILTER (WHERE user.id = ?1 AND otherUser.id = ?2 AND member is not null AND memberRequest is not null) AS members, " +
                "COUNT(*) FILTER (WHERE user.id = ?1 AND otherUser.id = ?2 AND member is null AND memberRequest is not null) AS pendings  " +
                "FROM Follow", id.userId, id.otherUserId)
        .project(MemberCount.class).firstResult();
  }

  // "SELECT COUNT(*) FILTER (WHERE otherUser = ?1 AND member is not null) AS followings, COUNT(*) FILTER (WHERE user = ?1 AND member is not null) AS followers, COUNT(*) FILTER (WHERE user = ?1 AND member is null AND memberRequest is not null) AS pending FROM Follow", user)
  //         "SELECT COUNT(*) FILTER (WHERE otherUser.id = ?1 AND user.id = ?2 AND member is not null)) AS followings, COUNT(*) FILTER (WHERE user.id = ?1 AND otherUser.id = ?2 AND member is not null) AS followers, COUNT(*) FILTER (WHERE user.id = ?1 AND otherUser.id = ?2 AND member is null AND memberRequest is not null) AS pending FROM Follow", id.userId, id.otherUserId)

  @Deprecated
  public static PanacheQuery<Follower> findFollowersOf(User user, User isFollowing) {
    return Follow.find("select new io.github.jelilio.smbackend.newsfeed.entity.projection.Follower(u.id, u.name, u.email, f.createdDate, (select count(f2.id) from Follow f2 where f2.otherUser = ?1 and f2.user = f.otherUser) from Follow f, User u where f.otherUser.id = u.id and f.user = ?1", isFollowing)
        .project(Follower.class);
  }

  public boolean isMember() {
    return member != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Follow follow = (Follow) o;
    return Objects.equals(id, follow.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}

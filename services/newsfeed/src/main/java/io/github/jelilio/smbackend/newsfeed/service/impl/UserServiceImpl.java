package io.github.jelilio.smbackend.newsfeed.service.impl;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.UserBioUpdateDto;
import io.github.jelilio.smbackend.common.dto.response.*;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.BadRequestException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.common.utils.Pair;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.NotificationType;
import io.github.jelilio.smbackend.newsfeed.client.UmAccountProxy;
import io.github.jelilio.smbackend.newsfeed.entity.ClubRequest;
import io.github.jelilio.smbackend.newsfeed.entity.Follow;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.key.FollowId;
import io.github.jelilio.smbackend.newsfeed.entity.projection.*;
import io.github.jelilio.smbackend.newsfeed.service.NotificationService;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserServiceImpl implements UserService {
  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  public static final String followingQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Following(u.id, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f2 WHERE f2.otherUser.id = ?1 and f2.user.id = f.otherUser.id and f2.member is not null and f2.memberRequest is null), " +
          "(select count(*) > 0 from Follow f3 WHERE f3.user.id = ?1 and f3.otherUser.id = f.otherUser.id and f3.member is not null and f3.memberRequest is null)) " +
          "from Follow f, User u where f.otherUser.id = u.id and f.user.id = ?2 and f.member is not null and f.memberRequest is null " +
          "order by f.createdDate desc";

  public static final String followerQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Follower( u.id, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f2 WHERE f2.user.id = ?1 and f2.otherUser.id = f.user.id and f2.member is not null and f2.memberRequest is null), " +
          "(select count(*) > 0 from Follow f3 WHERE f3.otherUser.id = ?1 and f3.user.id = f.user.id and f3.member is not null and f3.memberRequest is null)) " +
          "from Follow f, User u where f.user.id = u.id and f.otherUser.id = ?2 and f.member is not null and f.memberRequest is null " +
          "order by f.createdDate desc";

  public static final String pendingsQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Member( u.id, ?2, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = ?1 and f5.user.id = f.user.id and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f4 WHERE f4.otherUser.id = ?1 and f4.user.id = f.user.id and f4.member is null and f4.memberRequest is not null)) " +
          "from Follow f, User u where f.user.id = u.id and f.otherUser.id = ?2 and f.member is null and f.memberRequest is not null " +
          "order by f.createdDate desc";

  public static final String pageFollowingQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Following(u.id, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f2 WHERE f2.otherUser.id = ?1 and f2.user.id = f.otherUser.id and f2.member is not null and f2.memberRequest is null), " + // youFollowing
          "(select count(*) > 0 from Follow f3 WHERE f3.user.id = ?1 and f3.otherUser.id = f.otherUser.id and f3.member is not null and f3.memberRequest is null)) " + // itsFollowing
          "from Follow f, User u where f.otherUser.id = u.id and f.user.id = ?2 and f.member is not null and f.memberRequest is null " +
          "order by f.createdDate desc limit ?3 offset ?4";

  public static final String pageFollowingCommunitiesClubsQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Following(u.id, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f2 WHERE f2.otherUser.id = ?1 and f2.user.id = f.otherUser.id and f2.member is not null and f2.memberRequest is not null), " + // youFollowing
          "(select count(*) > 0 from Follow f3 WHERE f3.user.id = ?1 and f3.otherUser.id = f.otherUser.id and f3.member is not null and f3.memberRequest is not null)) " + // itsFollowing
          "from Follow f, User u where f.otherUser.id = u.id and f.user.id = ?2 and f.member is not null and f.memberRequest is not null " +
          "order by f.createdDate desc limit ?3 offset ?4";

  public static final String pageFollowingCommunitiesOrClubsQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.FollowingCommunityClub(u.id, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) from Follow f6 WHERE f6.otherUser.id = u.id and f6.member is not null and f6.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f2 WHERE f2.otherUser.id = ?1 and f2.user.id = f.otherUser.id and f2.member is not null and f2.memberRequest is not null), " + // youFollowing
          "(select count(*) > 0 from Follow f3 WHERE f3.user.id = ?1 and f3.otherUser.id = f.otherUser.id and f3.member is not null and f3.memberRequest is not null)) " + // itsFollowing
          "from Follow f, User u where f.otherUser.id = u.id and f.user.id = ?2 and f.otherUser.type = ?3 and f.member is not null and f.memberRequest is not null " +
          "order by f.createdDate desc limit ?4 offset ?5";

  public static final String pageFollowerQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Follower(u.id, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f2 WHERE f2.user.id = ?1 and f2.otherUser.id = f.user.id and f2.member is not null and f2.memberRequest is null), " +
          "(select count(*) > 0 from Follow f3 WHERE f3.otherUser.id = ?1 and f3.user.id = f.user.id and f3.member is not null and f3.memberRequest is null)) " +
          "from Follow f, User u where f.user.id = u.id and f.otherUser.id = ?2 and f.member is not null and f.memberRequest is null " +
          "order by f.createdDate desc limit ?3 offset ?4";

  public static final String pagePendingsQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Member(u.id, ?2, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = ?1 and f5.user.id = f.user.id and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f4 WHERE f4.otherUser.id = ?1 and f4.user.id = f.user.id and f4.member is null and f4.memberRequest is not null)) " +
          "from Follow f, User u where f.user.id = u.id and f.otherUser.id = ?2 and f.member is null and f.memberRequest is not null " +
          "order by f.createdDate desc limit ?3 offset ?4";

  public static final String pagePendingsQuery2 =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Member(u.id, ?2, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
          "(select count(*) > 0 from Follow f5 WHERE f5.user.id = ?1 and f5.user.id = f.user.id and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f4 WHERE f4.user.id = ?1 and f4.user.id = f.user.id and f4.member is null and f4.memberRequest is not null)) " +
          "from Follow f, User u where f.user.id = u.id and f.otherUser.id = ?2 and f.member is null and f.memberRequest is not null " +
          "order by f.createdDate desc limit ?3 offset ?4";

  public static final String pageMembersQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Member(u.id, ?2, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
//          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = ?1 and f5.user.id = f.user.id and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = ?1 and f5.user.id = f.user.id and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f4 WHERE f4.otherUser.id = ?1 and f4.user.id = f.user.id and f4.member is null and f4.memberRequest is not null)) " +
          "from Follow f, User u where f.user.id = u.id and f.otherUser.id = ?2 and f.member is not null and f.memberRequest is not null " +
          "order by f.createdDate desc limit ?3 offset ?4";

  public static final String pageMembersQuery2 =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Follower(u.id, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, f.createdDate, " +
//          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = ?1 and f5.user.id = f.user.id and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f2 WHERE f2.user.id = ?1 and f2.otherUser.id = f.user.id and f2.member is not null and f2.memberRequest is null), " +
          "(select count(*) > 0 from Follow f3 WHERE f3.otherUser.id = ?1 and f3.user.id = f.user.id and f3.member is not null and f3.memberRequest is null)) " +

//          "(select count(*) > 0 from Follow f5 WHERE f5.user.id = ?1 and f5.user.id = f.user.id and f5.member is not null and f5.memberRequest is not null), " +
//          "(select count(*) > 0 from Follow f4 WHERE f4.user.id = ?1 and f4.user.id = f.user.id and f4.member is null and f4.memberRequest is not null)) " +
          "from Follow f, User u where f.user.id = u.id and f.otherUser.id = ?2 and f.member is not null and f.memberRequest is not null " +
          "order by f.createdDate desc limit ?3 offset ?4";

  public static final String pageCommunitiesQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Community( u.id, ?1, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, u.createdDate, " +
          " c2.id, c2.name, c2.username, " +
          "(select count(*) from Follow f6 WHERE f6.otherUser.id = u.id and f6.member is not null and f6.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = u.id and f5.user.id = ?1 and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f4 WHERE f4.otherUser.id = u.id and f4.user.id = ?1 and f4.member is null and f4.memberRequest is not null)) " +
          "from User u, User c2 where c2.id = u.owner.id and u.type = ?2" +
          "order by u.createdDate desc limit ?3 offset ?4";

  public static final String pageCommunityClubsQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.Club( u.id, ?1, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, u.createdDate, " +
          " c2.id, c2.name, c2.username, r3.id, r3.name, r3.username, " +
          "(select count(*) from Follow f6 WHERE f6.otherUser.id = u.id and f6.member is not null and f6.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = u.id and f5.user.id = ?1 and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f4 WHERE f4.otherUser.id = u.id and f4.user.id = ?1 and f4.member is null and f4.memberRequest is not null)) " +
          "from User u, User c2, User r3 where u.type = ?2 and c2.id = u.owner.id and r3.id = u.requester.id and u.owner.id = ?3 " +
          "order by u.createdDate desc limit ?4 offset ?5";

  // boolean youFollowing, boolean itsFollowing, boolean itsMember, boolean itsPending

  private static final String allSearchUsersPaginationQuery =
      "select new io.github.jelilio.smbackend.newsfeed.entity.projection.UserCommunity( u.id, ?1, u.name, u.email, u.username, u.avatarUrl, u.avatarType, u.type, u.verifiedDate, u.createdDate, " +
//          " c2.id, c2.name, c2.username, " +
          "(select count(*) from Follow f6 WHERE f6.otherUser.id = u.id and f6.member is not null and f6.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f2 WHERE f2.otherUser.id = ?1 and f2.user.id = u.id and f2.member is not null and f2.memberRequest is not null), " + // youFollowing
          "(select count(*) > 0 from Follow f3 WHERE f3.otherUser.id = u.id and f3.user.id = ?1 and f3.member is not null and f3.memberRequest is not null), " + // itsFollowing
          "(select count(*) > 0 from Follow f5 WHERE f5.otherUser.id = u.id and f5.user.id = ?1 and f5.member is not null and f5.memberRequest is not null), " +
          "(select count(*) > 0 from Follow f4 WHERE f4.otherUser.id = u.id and f4.user.id = ?1 and f4.member is null and f4.memberRequest is not null)) " +
          "from User u where (lower(u.name) like ?2 or lower(u.username) like ?2) and u.type in ?3" +
          "order by u.createdDate desc limit ?4 offset ?5";

  @Inject
  @RestClient
  UmAccountProxy userManagerApi;

  @Inject
  Mutiny.SessionFactory sf;

  @Inject
  NotificationService notificationService;

  @Override
  public Uni<Paged<UserCommunity>> searchAll(String userId, String text, int size, int index) {
    Page page = Page.of(index, size);
    Uni<User> userUni = findById(userId);

    return userUni.flatMap(user -> {
      var q = "%" + text.toLowerCase() + "%";
      var types = List.of(UserType.STUDENT, UserType.STAFF, UserType.ALUMNI, UserType.COMMUNITY);
      var count = User.countAllUser(q, types);
      Uni<List<UserCommunity>> list =  sf.withSession( session -> {
        var query = session
            .createQuery(allSearchUsersPaginationQuery, UserCommunity.class)
            .setParameter(1, user.id)
            .setParameter(2, q)
            .setParameter(3, types)
            .setParameter(4, size)
            .setParameter(5, index*size);

        return query.getResultList();
      });

      return PaginationUtil.paginate(page, list, count);
    });
  }

  @Override
  public Uni<UserProfile> profile(String userId) {
    return findById(userId).flatMap(user -> {
      if(user.type == UserType.COMMUNITY || user.type == UserType.CLUB) {
        return Follow.countMemberByUserId(user)
            .flatMap(memberCount -> {
              return countCommunityClubs(user.id.toString())
                  .map(clubCount -> {
                    return new UserProfile.UserProfileBuilder(user.id.toString(), user.type, user.name, user.username,
                        user.bio, user.avatarUrl, user.avatarType, user.enabled, user.createdDate, user.verifiedDate,
                        user.birthDate)
                        .setMemberCount(new MemberStatusCount(memberCount.pendings, memberCount.members))
                        .setClubCount(new ClubStatusCount(clubCount.first(), clubCount.second()))
                        .setOwner(user.ownerRes())
                        .build();
                  });
            });
      }

      return Follow.countFollowByUserId(user)
          .map(followCount -> {
            return new UserProfile.UserProfileBuilder(user.id.toString(), user.type, user.name, user.username,
                user.bio, user.avatarUrl, user.avatarType, user.enabled, user.createdDate, user.verifiedDate,
                user.birthDate)
                .setIdNumber(user.idNumber)
                .setFollowCount(new FollowStatusCount(followCount.followings, followCount.followers, followCount.communities, followCount.clubs))
                .setOwner(user.ownerRes())
                .build();
          });
    });
  }

  @Override
  public Uni<UserProfile> profile(String userId, User loggedInUser) {
    String loggedInUserId = loggedInUser.id.toString();

    return findById(userId).flatMap(user -> {
      if(user.type == UserType.COMMUNITY) {
        return Follow.countMemberByUserId(user)
            .flatMap(memberCount -> {
              return countCommunityClubs(user.id.toString())
                  .flatMap(clubCount -> withMemberStatus(userId, loggedInUserId, user, memberCount, clubCount));
            });
      }

      if(user.type == UserType.CLUB) {
        return Follow.countMemberByUserId(user)
            .flatMap(memberCount -> {
              return countCommunityClubs(user.id.toString())
                  .flatMap(clubCount -> withClubMemberStatus(userId, loggedInUserId, user, memberCount, clubCount));
            });
      }

      if(loggedInUser.type == UserType.COMMUNITY || loggedInUser.type == UserType.CLUB ) {
        return Follow.countFollowByUserId(user)
            .flatMap(followCount -> withMemberStatus(userId, loggedInUserId, user, followCount));
      }

      return Follow.countFollowByUserId(user)
          .flatMap(followCount -> withStatus(userId, loggedInUserId, user, followCount));
    });
  }

  // followStatus
  public Uni<UserProfile> withStatus(String userId, String loggedInUserId, User user, FollowCount followCount) {
    return followStatus(loggedInUserId, userId)
        .map(followStatus -> {
          return new UserProfile.UserProfileBuilder(user.id.toString(), user.type, user.name, user.username,
              user.bio, user.avatarUrl, user.avatarType, user.enabled, user.createdDate, user.verifiedDate,
              user.birthDate)
              .setFollowCount(new FollowStatusCount(followCount.followings, followCount.followers, followCount.communities, followCount.clubs))
              .setFollowStatus(followStatus)
              .setOwner(user.ownerRes())
              .build();
        });
  }

  public Uni<UserProfile> withMemberStatus(String userId, String loggedInUserId, User user, FollowCount followCount) {
    return memberStatus(userId, loggedInUserId)
        .map(memberStatus -> {
          return new UserProfile.UserProfileBuilder(user.id.toString(), user.type, user.name, user.username,
              user.bio, user.avatarUrl, user.avatarType, user.enabled, user.createdDate, user.verifiedDate,
              user.birthDate)
              .setFollowCount(new FollowStatusCount(followCount.followings, followCount.followers, followCount.communities, followCount.clubs))
              .setMemberStatus(memberStatus)
              .setOwner(user.ownerRes())
              .build();
        });
  }

  public Uni<UserProfile> withMemberStatus(String userId, String loggedInUserId, User user, MemberCount memberCount, Pair<Long, Long> clubCount) {
    return memberStatus(loggedInUserId, userId)
        .map(memberStatus -> {
          return new UserProfile.UserProfileBuilder(user.id.toString(), user.type, user.name, user.username,
              user.bio, user.avatarUrl, user.avatarType, user.enabled, user.createdDate, user.verifiedDate,
              user.birthDate)
              .setMemberStatus(memberStatus)
              .setMemberCount(new MemberStatusCount(memberCount.pendings, memberCount.members))
              .setClubCount(new ClubStatusCount(clubCount.first(), clubCount.second()))
              .setOwner(user.ownerRes())
              .build();
        });
  }

  public Uni<UserProfile> withClubMemberStatus(String userId, String loggedInUserId, User user, MemberCount memberCount, Pair<Long, Long> clubCount) {
    return memberStatus(loggedInUserId, userId)
        .map(memberStatus -> {
          return new UserProfile.UserProfileBuilder(user.id.toString(), user.type, user.name, user.username,
              user.bio, user.avatarUrl, user.avatarType, user.enabled, user.createdDate, user.verifiedDate,
              user.birthDate)
              .setMemberCount(new MemberStatusCount(memberCount.pendings, memberCount.members))
              .setClubCount(new ClubStatusCount(clubCount.first(), clubCount.second()))
//              .setMemberStatus(memberStatus) // to verified
              .setClubStatus(memberStatus)
              .setOwner(user.ownerRes())
              .build();
        });
  }

  @Override
  public Uni<List<User>> findAll() {
    return User.findAll().list();
  }

  @Override
  public Uni<Paged<User>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, User.findAll().page(page));
  }

  @Override
  public Uni<User> findById(String id) {
    return User.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  @Override
  public Uni<User> findByIdOrNull(UUID id) {
    return User.findById(id);
  }

  @Override
  public Uni<User> findByIdAndType(String id, UserType type) {
    return User.findByIdAndType(id, type).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  @Override
  public Uni<User> findByCommunity(String id) {
    return User.findByIdAndType(id, UserType.COMMUNITY).onItem().ifNull()
        .failWith(() -> new NotFoundException("Community Not found"));
  }

  private Uni<User> findByIdCommunityOrClub(String id) {
    return User.findByIdCommunityOrClub(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Community or Club, Not found"));
  }

  public Uni<ClubRequest> findClubRequest(String id) {
    return ClubRequest.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Club's request Not found"));
  }

  @Override
  public Uni<User> save(RegisterRes register, RegisterCommunityDto dto, User owner, UserType type) {
    logger.debug("persisting community {} to database", register.name());

    return User.findById(register.id())
        .replaceIfNullWith(new User(register.id(), register.name(), register.email(), register.username()))
        .flatMap(user -> {
          user.type = type;
          user.owner = owner;
          user.bio = dto.bio();
          user.enabled = register.enabled();
          return Panache.withTransaction(user::persist);
        });
  }

  @Deprecated
  public Uni<User> saveClub(RegisterRes register, User community, String clubRequestId) {
    logger.debug("saveClub: persisting {} to database", register.name());

    return User.findById(register.id())
        .replaceIfNullWith(new User(register.id(), register.name(), register.email()))
        .flatMap(club ->
            updateRequester(community, clubRequestId).flatMap(reqOrCom -> {
              logger.debug("saveClub: reqOrCom is: {}", reqOrCom);
              club.type = UserType.CLUB;
              club.owner = community;
              club.requester = reqOrCom;
              return Panache.withTransaction(club::persist);
            }));
  }

  public Uni<User> updateRequester(User community, String clubRequestId) {
    logger.debug("updateRequester: community: {}, clubRequestId: {}", community, clubRequestId);
    if(clubRequestId == null) return Uni.createFrom().item(community);

    return findClubRequest(clubRequestId)
        .flatMap(request -> {
          logger.debug("updateRequester: clubRequest is found: {}", request);
          if(request.isApproved()) {
            return Uni.createFrom().failure(() -> new BadRequestException("Club's Request already approved"));
          }

          request.approvalDate = Instant.now();
          return Panache.<ClubRequest>withTransaction(request::persist).map(it -> it.requester);
        });
  }

  @Override
  public Uni<User> save(RegisterRes register) {
    logger.debug("persisting {} to database", register.name());

    return User.findById(register.id())
        .replaceIfNullWith(new User(register.id(), register.name(), register.email(), register.enabled(), register.userType()))
        .flatMap(user -> {
          user.enabled = register.enabled();
          if(register.username() != null) {
            user.username = register.username();
          }
          return Panache.withTransaction(user::persist);
        });
  }

  @Override
  public Uni<User> updateUserInfo(UserUpdatedRes userUpdate) {
    return User.findById(userUpdate.id)
        .flatMap(user -> {
          user.avatarType = userUpdate.imageType != null? userUpdate.imageType : user.avatarType;
          user.avatarUrl = userUpdate.imageUrl != null? userUpdate.imageUrl : user.avatarUrl;
          return Panache.withTransaction(user::persist);
        });
  }

  public Uni<Pair<Long, Long>> countCommunityClubs(String communityId) {
    return ClubRequest.countByCommunityAndPending(communityId)
        .flatMap(pendings -> User.countCommunityClubs_(communityId, UserType.CLUB)
            .map(clubsCount -> Pair.of(pendings, clubsCount)));
  }

  public Uni<FollowStatus> followStatus(String loggedInUser, String otherUser) {
    logger.info("followStatus");
    Uni<FollowOnlyCount> followCountUni = Follow.checkFollowById(new FollowId(loggedInUser, otherUser));
    return followCountUni.map(it -> new FollowStatus( it.followings  > 0, it.followers > 0));
  }

  public Uni<MemberStatus> memberStatus(String loggedInUser, String otherUser) {
    logger.info("memberStatus");
    Uni<MemberCount> followCountUni = Follow.checkMemberById(new FollowId(loggedInUser, otherUser));
    return followCountUni.map(it -> new MemberStatus(it.members > 0, it.pendings > 0));
  }

  public Uni<Boolean> isUserCommunityOrClub(String userId) {
    return User.findById(userId).flatMap(user -> {
      if(user == null) {
        return Uni.createFrom().failure(() -> new NotFoundException("User not found"));
      }

     return Uni.createFrom().item(UserType.COMMUNITY == user.type || UserType.CLUB == user.type);
    });
  }


  @Override
  public Uni<Follow> follow(String userId, String otherUserId) {
    if(userId.equalsIgnoreCase(otherUserId)) {
      return Uni.createFrom().failure(new AlreadyExistException("Sorry, you cannot follow or request membership from oneself"));
    }

//    Uni<User> uniUser = User.findById(userId);
//    Uni<User> uniOtherUser = User.findById(otherUserId);

    return Panache.withTransaction(() -> {
      return isUserCommunityOrClub(otherUserId).flatMap(isOtherUserCommunityOrClub -> {
        if(isOtherUserCommunityOrClub) {
          logger.info("Other user is a community or a club: {}", otherUserId);
          return requestForMembership(userId, otherUserId);
        }

        return User.findById(userId).flatMap(user -> {
          return User.findById(otherUserId).flatMap(otherUser -> {
            Follow follow = new Follow(user, otherUser); // me (user) following you (otherUser)
            follow.member = Instant.now();
            return checkIfAlreadyFollowing(follow.id)
                .flatMap(__ -> Panache.<Follow>withTransaction(follow::persist))
                .flatMap(persisted -> {
                  return notificationService.createNotification(NotificationType.FOLLOW, user, otherUser)
                      .map(__ -> persisted);
                });
          });
        });
      });
    });
  }

  // request for community or club membership
  @Override
  public Uni<Follow> requestForMembership(String userId, String communityId) {
    if(userId.equalsIgnoreCase(communityId)) {
      return Uni.createFrom().failure(new AlreadyExistException("Sorry, you cannot request membership from oneself"));
    }

    Uni<User> uniUser = findById(userId);
    Uni<User> uniOtherUser = findByIdCommunityOrClub(communityId); //prev1: findByCommunity, work for community only; find prev findById, work for any user

    return Panache.withTransaction(() -> {
      return uniUser.flatMap(user -> {
        return uniOtherUser.flatMap(otherUser -> {
          FollowId id = new FollowId(user.id, otherUser.id); // me (user) requesting to be a member of you (otherUser)

          return checkIfAlreadyAMemberOrRequestSent(id)
              .flatMap(__ -> Follow.<Follow>findById(id)
                  .replaceIfNullWith(new Follow(user, otherUser)))// me (user) requesting to be a member of you (otherUser)
              .flatMap(result -> {
                result.memberRequest = Instant.now();
                return Panache.<Follow>withTransaction(result::persist)
                    .flatMap(persisted -> {
                      return notificationService.createNotification(NotificationType.REQUEST, user, otherUser)
                          .map(__ -> persisted);
                    });
              });
        });
      });
    });
  }

  @Override
  public Uni<Boolean> unfollow(String userId, String otherUserId) {
    Uni<User> uniUser = User.findById(userId);
    Uni<User> uniOtherUser = User.findById(otherUserId);

    return Panache.withTransaction(() -> {
      return uniUser.flatMap(user -> {
        return uniOtherUser.flatMap(otherUser -> {
          Follow follow = new Follow(user, otherUser);

          return checkIfNotFollowing(follow.id)
              .flatMap(__ -> Follow.deleteById(follow.id));
        });
      });
    });
  }

  @Override
  public Uni<List<User>> followings(String userId) {
    return Follow.findFollowings(userId).list()
        .map(follows ->
            follows.stream()
                .map(it -> it.otherUser)
                .collect(Collectors.toList())
        );
  }

  @Override
  public Uni<List<User>> followingCommunities(String userId) {
    return Follow.findFollowingCommunities(userId).list()
        .map(follows ->
            follows.stream()
                .map(it -> it.otherUser)
                .collect(Collectors.toList())
        );
  }

//  @Override
  public Uni<Paged<Following>> followings_not_working(String userId, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Follow.findFollowings_(userId).page(page));
  }

  @Override
  @Deprecated
  public Uni<List<User>> followers(String userId) {
    return Follow.findFollowers(userId).list()
        .map(follows ->
            follows.stream()
                .map(it -> it.user)
                .collect(Collectors.toList())
        );
  }



  @Override
  @Deprecated
  public Uni<List<User>> members(String userId) {
    return Follow.findFollowers(userId).list()
        .map(follows ->
            follows.stream()
                .map(it -> it.user)
                .collect(Collectors.toList())
        );
  }

//  @Override
  @Deprecated
  public Uni<Paged<Follower>> followers_old(String userId, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Follow.findFollowers_(userId).page(page));
  }

//  @Override
  @Deprecated
  public Uni<Paged<Follower>> followers_no_working(String userId, int size, int index) {
    Page page = Page.of(index, size);

    return findById(userId).flatMap(user -> {
      return PaginationUtil.paginate(page, Follow.findFollowersOf(user, user).page(page));
    });
  }

  @Override
  public Uni<List<Follower>> allFollowings(String userId) {
    return sf.withSession( session -> {
      var query = session
          .createQuery( followingQuery, Follower.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId));
      return query.getResultList();
    });
  }

  @Override
  public Uni<List<Follower>> allFollowers(String userId) {
    return sf.withSession( session -> {
      var query = session
          .createQuery( followerQuery, Follower.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId));
      return query.getResultList();
    });
  }

  @Override
  public Uni<List<Member>> allPendings(String userId) {
    return sf.withSession( session -> {
      var query = session
          .createQuery( pendingsQuery, Member.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId));
      return query.getResultList();
    });
  }

  @Override
  public Uni<Paged<Following>> followings(String userId, int size, int index) {
    logger.info("followings, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countFollowings_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageFollowingQuery , Following.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Following>> followingsCommunitiesClubs(String userId, int size, int index) {
    logger.info("followingsCommunitiesClubs, hql; {}", userId);
    Page page = Page.of(index, size);

    var count = Follow.countFollowingsCommunitiesClubs_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageFollowingCommunitiesClubsQuery , Following.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Following>> followingsCommunitiesOrClubs(String userId, UserType type, int size, int index) {
    logger.info("followingsCommunitiesOrClubs, hql; {}", userId);
    Page page = Page.of(index, size);

    var count = Follow.countFollowingsCommunitiesOrClubs_(userId, type);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageFollowingCommunitiesOrClubsQuery , Following.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, type)
          .setParameter(4, size)
          .setParameter(5, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Following>> followings(String loggedInUserId, String userId, int size, int index) {
    logger.info("followings, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countFollowings_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageFollowingQuery , Following.class)
          .setParameter(1, UUID.fromString(loggedInUserId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Follower>> followers(String userId, int size, int index) {
    logger.info("followers, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countFollowers_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageFollowerQuery , Follower.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Member>> pendings(String userId, int size, int index) {
    logger.info("pendings, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countPendings_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pagePendingsQuery , Member.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Member>> members(String userId, int size, int index) {
    logger.info("members, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countMembers_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageMembersQuery , Member.class)
          .setParameter(1, UUID.fromString(userId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Follower>> followers(String loggedInUserId, String userId, int size, int index) {
    logger.info("followers, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countFollowers_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageFollowerQuery , Follower.class)
          .setParameter(1, UUID.fromString(loggedInUserId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Member>> pendings(String loggedInUserId, String userId, int size, int index) {
    logger.info("followers, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countPendings_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pagePendingsQuery2 , Member.class)
          .setParameter(1, UUID.fromString(loggedInUserId))
          .setParameter(2, UUID.fromString(userId))
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Follower>> members(String loggedInUserId, String userId, int size, int index) {
    logger.info("members, hql; {}", userId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = Follow.countMembers_(userId);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageMembersQuery2 , Follower.class)
          .setParameter(1, UUID.fromString(loggedInUserId))
          .setParameter(2, UUID.fromString(userId)) // communityId or clubId
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Community>> communities(String loggedInUserId, int size, int index) {
    logger.info("communities, hql; {}", loggedInUserId);
    Page page = Page.of(index, size);

//    var query = Post.findAllPosts(size, index*size).project(PostReal.class);
    var count = User.countCommunities_(UserType.COMMUNITY);
    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageCommunitiesQuery , Community.class)
          .setParameter(1, UUID.fromString(loggedInUserId))
          .setParameter(2, UserType.COMMUNITY)
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, count);
  }

  @Override
  public Uni<Paged<Community>> clubs(String loggedInUserId, int size, int index) {
    logger.info("clubs, hql; {}", loggedInUserId);
    Page page = Page.of(index, size);

    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageCommunitiesQuery , Community.class)
          .setParameter(1, UUID.fromString(loggedInUserId))
          .setParameter(2, UserType.CLUB)
          .setParameter(3, size)
          .setParameter(4, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, User.countCommunities_(UserType.CLUB));
  }

  @Override
  public Uni<Paged<Club>> clubs(String loggedInUserId, String communityId, int size, int index) {
    logger.info("clubs, hql; {}", loggedInUserId);
    Page page = Page.of(index, size);

    var list =  sf.withSession( session -> {
      var query = session
          .createQuery( pageCommunityClubsQuery , Club.class)
          .setParameter(1, UUID.fromString(loggedInUserId))
          .setParameter(2, UserType.CLUB)
          .setParameter(3, UUID.fromString(communityId))
          .setParameter(4, size)
          .setParameter(5, index*size);
      return query.getResultList();
    });

    return PaginationUtil.paginate(page, list, User.countCommunityClubs_(communityId, UserType.CLUB));
  }

  // find all members of a community or club
  @Override
  public Uni<List<Member>> members_(String userId) {
    return Follow.findMembers_(userId).list();
  }

//  @Override
//  public Uni<Paged<Member>> members(String communityId, int size, int index) {
//    Page page = Page.of(index, size);
//
//    return PaginationUtil.paginate(page, Follow.findMembers_(communityId).page(page));
//  }

  @Override
  public Uni<Paged<Member>> memberRequests(String communityId, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Follow.findMemberRequests_(communityId).page(page));
  }

  // find all communities a member belongs to
//  @Override
//  public Uni<List<Community>> communities_(String memberId) {
//    return Follow.findCommunities_(memberId).list();
//  }
//
//  @Override
//  public Uni<Paged<Community>> communities(String memberId, int size, int index) {
//    Page page = Page.of(index, size);
//
//    return PaginationUtil.paginate(page, Follow.findCommunities_(memberId).page(page));
//  }

  public Uni<Boolean> checkIfAlreadyFollowing(FollowId id) {
    return Follow.countById(id)
        .flatMap(count -> {
          if (count > 0) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Already following this user"));
          }

          return Uni.createFrom().item(false);
        });
  }

  public Uni<Boolean> checkIfAlreadyFollowingOnly(FollowId id) {
    return Follow.countById(id)
        .flatMap(count -> Uni.createFrom().item(count > 0));
  }

  public Uni<Follow> findByIdOnly(FollowId id) {
    return Follow.<Follow>findById(id).onItem().ifNull()
        .continueWith((Follow) new Follow());
  }

  public Uni<Boolean> checkIfAlreadyAMemberOrRequestSent(FollowId id) {
    return Follow.countByAlreadyAMember(id)
        .flatMap(count -> {
          if (count > 0) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Already a member"));
          }

          return Follow.countByAlreadySentARequest(id);
        }).flatMap(count -> {
          if (count > 0) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Membership request already sent"));
          }

          return Uni.createFrom().item(false);
        });
  }

  @Override
  public Uni<Boolean> checkIfAlreadyAMember(User community, User requester) {
    FollowId id = new FollowId(requester.id, community.id);

    return Follow.countByAlreadyAMember(id).map(it -> it > 0);
  }

  public Uni<Boolean> checkIfNotFollowing(FollowId id) {
    return Follow.countById(id)
        .flatMap(count -> {
          if (count == 0) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("You're not following this user"));
          }

          return Uni.createFrom().item(Boolean.FALSE);
        });
  }

  @Override
  public Uni<Paged<ClubRequest>> clubRequests(String communityId, int size, int index) {
    Page page = Page.of(index, size);

    return findByCommunity(communityId).flatMap(community ->
        PaginationUtil.paginate(page, ClubRequest.findPendingsByCommunity(community).page(page)));
  }

  @Override
  public Uni<Paged<ClubRequest>> clubAllRequests(String communityId, int size, int index) {
    Page page = Page.of(index, size);

    return findByCommunity(communityId).flatMap(community ->
        PaginationUtil.paginate(page, ClubRequest.findByCommunity(community).page(page)));
  }

  @Override
  public Uni<User> updateNameBio(User user, UserBioUpdateDto dto) {
    return Panache.withTransaction(() -> {
      user.name = dto.name();
      user.bio = dto.bio();
      user.username = dto.username() == null || dto.username().isBlank() ? user.username : dto.username();
      user.birthDate = dto.birthDate();
      user.idNumber = dto.idNumber() == null || dto.idNumber().isBlank() ? user.idNumber : dto.idNumber();
      return Panache.withTransaction(user::persist);
    });
  }

  @Override
  public Uni<User> updateAvatar(User user, PhotoRes dto) {
    return Panache.withTransaction(() -> {
      user.avatarType = dto.avatarType();
      user.avatarUrl = dto.avatarUrl();
      return Panache.withTransaction(user::persist);
    });
  }

  // fetch by type
  @Override
  public Uni<Paged<User>> findAllByType(UserType type, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, User.findAllByType(type).page(page));
  }

  // fetch all communities
  @Override
  public Uni<Paged<User>> findAllCommunities(int size, int index) {
    return findAllByType(UserType.COMMUNITY, size, index);
  }

  @Override
  public Uni<Boolean> approveVerification(String loggedInUsername, User user) {
    user.verifiedDate = Instant.now();
    user.verifiedBy = loggedInUsername;
    return Panache.withTransaction(user::persist)
        .flatMap(__ -> Uni.createFrom().item(true));
  }

  @Override
  public Uni<Void> requestVerification(User user) {
    user.requestDate = Instant.now();
    return Panache.withTransaction(user::persist)
        .flatMap(__ -> Uni.createFrom().voidItem());
  }

  @Override
  public Uni<User> disableOrEnableUser(String id, Boolean value) {
    Uni<User> userUni = findById(id);

    return Panache.withTransaction(() ->
        userUni.flatMap(extUser -> {
          if(extUser.enabled && value) {
            return Uni.createFrom().item(extUser);
          }

          if(!extUser.enabled && !value) {
            return Uni.createFrom().item(extUser);
          }

          extUser.enabled = value;
          return Panache.withTransaction(extUser::persist);
        })
    );
  }
}

package io.github.jelilio.smbackend.newsfeed.service.impl;

import io.github.jelilio.smbackend.common.dto.ClubRequestDto;
import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.BadRequestException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.newsfeed.client.UmAccountProxy;
import io.github.jelilio.smbackend.newsfeed.entity.ClubRequest;
import io.github.jelilio.smbackend.newsfeed.entity.Follow;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.key.FollowId;
import io.github.jelilio.smbackend.newsfeed.entity.projection.Community;
import io.github.jelilio.smbackend.newsfeed.service.CommunityService;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@ApplicationScoped
public class CommunityServiceImpl implements CommunityService {
  private static final Logger logger = LoggerFactory.getLogger(CommunityServiceImpl.class);

  @Inject
  @RestClient
  UmAccountProxy userManagerApi;

  @Inject
  UserService userService;

  @Override
  public Uni<User> createCommunity(String loggedInUserId, RegisterCommunityDto dto) {
    return userService.findById(loggedInUserId)
        .flatMap(user -> userManagerApi.registerCommunity(dto)
            .flatMap(res -> userService.save(res, dto, user, UserType.COMMUNITY)
                // add owner of the community as member of the community
                .flatMap(community -> addUserAsMember(user, community, false, true)
                    .map(follow -> community))));
  }

  public Uni<Follow> addUserAsMember(User owner, User communityOrClub, boolean requestRequire, boolean isOwner) {
    FollowId id = new FollowId(owner.id, communityOrClub.id);

    return checkIfAlreadyAMemberOrRequestAccepted(id, requestRequire)
        .flatMap(__ -> Follow.<Follow>findById(id)
            .replaceIfNullWith(new Follow(owner, communityOrClub)))// you (user) accepting my (otherUser) membership request
        .flatMap(result -> {
          if(isOwner) result.organizer = Instant.now();
          if(!requestRequire) result.memberRequest = Instant.now();

          result.member = Instant.now();

          return Panache.<Follow>withTransaction(result::persist)
              .flatMap(updated -> {
                communityOrClub.memberCount = communityOrClub.memberCount == null? 1 : communityOrClub.memberCount + 1; // increase memberCount
                return Panache.withTransaction(communityOrClub::persist)
                    .map(__ -> updated);
              });
        });
  }

  // for community and club
  @Override
  public Uni<Follow> acceptMembershipRequest(String loggedInCommunityId, String otherUserId) {
    if(loggedInCommunityId.equalsIgnoreCase(otherUserId)) {
      return Uni.createFrom().failure(new AlreadyExistException("Sorry, you can't request to be your own member"));
    }

    return Panache.withTransaction(() -> {
      return  findByIdCommunityOrClub(loggedInCommunityId).flatMap(user -> {
        return findByIdNonCommunityOrClub(otherUserId).flatMap(otherUser -> {
          FollowId id = new FollowId(otherUser.id, user.id); // you (user) accepting my (otherUser) membership request

          return addUserAsMember(otherUser, user, true, false);
          /*
          return checkIfAlreadyAMemberOrRequestAccepted(id, true)
              .flatMap(__ -> Follow.<Follow>findById(id)
                  .replaceIfNullWith(new Follow(otherUser, user)))// you (user) accepting my (otherUser) membership request
              .flatMap(result -> {
                result.member = Instant.now();
                return Panache.<Follow>withTransaction(result::persist)
                    .flatMap(updated -> {
                      user.memberCount = user.memberCount == null? 1 : user.memberCount + 1; // increase memberCount
                      return Panache.withTransaction(user::persist)
                          .map(__ -> updated);
                    });
              });
          */
        });
      });
    });
  }

  @Override
  public Uni<Follow> addMember(String userId, String otherUserId) {
    if(userId.equalsIgnoreCase(otherUserId)) {
      return Uni.createFrom().failure(new AlreadyExistException("Sorry, you cant be your own member"));
    }

    Uni<User> uniUser = findByIdCommunityOrClub(userId);
    Uni<User> uniOtherUser = findByIdNonCommunityOrClub(otherUserId);

    return Panache.withTransaction(() -> {
      return uniUser.flatMap(user -> {
        return uniOtherUser.flatMap(otherUser -> {
          return addUserAsMember(otherUser, user, false, false);

          /*
          FollowId id = new FollowId(otherUser.id, user.id);

          return checkIfAlreadyAMemberOrRequestAccepted(id, false)
              .flatMap(__ -> Follow.<Follow>findById(id)
                  .replaceIfNullWith(new Follow(otherUser, user)))
              .flatMap(result -> {
                result.member = Instant.now();
                return Panache.withTransaction(result::persist);
              });
          */
        });
      });
    });
  }

  private Uni<User> findByIdCommunityOrClub(String id) {
    return User.findByIdCommunityOrClub(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  private Uni<User> findByIdNonCommunityOrClub(String id) {
    return User.findByIdNonCommunityOrClub(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  public Uni<Boolean> checkIfAlreadyAMemberOrRequestAccepted(FollowId id, boolean requestRequire) {
    return Follow.countByAlreadyAMember(id)
        .flatMap(count -> {
          if (count > 0) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Already a member"));
          }

          // check if a request was sent
          return Follow.countByAlreadySentARequest(id);
        }).flatMap(count -> {
          if (count <= 0 && requestRequire) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("No Membership request was sent"));
          }

          return Uni.createFrom().item(false);
        });
  }

  @Override
  public Uni<Boolean> checkIfAlreadyAMember(User community, User requester) {
    FollowId id = new FollowId(requester.id, community.id);

    return Follow.countByAlreadyAMember(id).map(it -> it > 0);
  }

  @Override
  public Uni<Boolean> cancelMembershipRequest(String communityId, String otherUserId) {
    Uni<User> uniUser = User.findById(communityId);
    Uni<User> uniOtherUser = User.findById(otherUserId);

    return Panache.withTransaction(() -> {
      return uniUser.flatMap(user -> {
        return uniOtherUser.flatMap(otherUser -> {
          Follow follow = new Follow(otherUser, user);

          return checkIfAlreadyAMember(user, otherUser)
              .flatMap(result -> {
                return Follow.deleteById(follow.id)
                    .flatMap(res -> {
                      user.memberCount = user.memberCount == null? 0 : user.memberCount - 1; // decrease memberCount
                      return Panache.withTransaction(user::persist)
                          .map(__ -> res);
                    });
              });
        });
      });
    });
  }


  @Override
  public Uni<ClubRequest> clubRequest(String memberId, String communityId, ClubRequestDto dto) {
    Uni<User> uniRequester = userService.findById(memberId);
    Uni<User> uniCommunity = userService.findByCommunity(communityId);

    return Panache.withTransaction(() -> {
      return uniCommunity.flatMap(community -> uniRequester.flatMap(requester ->
          checkIfRequestExist(dto.name(), community).flatMap(itExist -> {
            if(itExist) {
              return Uni.createFrom().failure(new AlreadyExistException("Request of this name already exist"));
            }

            return checkIfAlreadyAMember(community, requester)
                .flatMap(it -> {
                  if(!it) {
                    return Uni.createFrom().failure(new AlreadyExistException("Only a member can request"));
                  }

                  ClubRequest clubRequest = new ClubRequest(dto.name(), dto.purpose(), requester, community);

                  return Panache.withTransaction(clubRequest::persist);
                });
          })));
    });
  }

  @Override
  public Uni<User> acceptClubRequest(String loggedInCommunityId, String requestId, RegisterCommunityDto dto) {
    return userService.findByCommunity(loggedInCommunityId)
        .flatMap(community -> {
          // find request
          return checkIfRequestExistById(requestId, community).flatMap(itExist -> {
            if(!itExist) {
              return Uni.createFrom().failure(new NotFoundException("Club's request Not found"));
            }
            return Uni.createFrom().item(true);
          }).flatMap(itExist -> userManagerApi.registerClub(dto)
                  .flatMap(res -> saveClub(res, dto, community, requestId))
              .flatMap(club -> addRequesterAsMemberToClub(club)));
        });
  }

  private Uni<User> addRequesterAsMemberToClub(User club) {
    User requester = club.requester == null? club.owner : club.requester; // if no requester, make community's owner as requester

    // add owner of the community as member of the community
    return addUserAsMember(requester, club, false, true)
        .map(__ -> club);
  }

  @Override
  public Uni<ClubRequest> rejectClubRequest(String loggedInCommunityId, String requestId) {
    return userService.findByCommunity(loggedInCommunityId)
        .flatMap(community -> {
          // find request
          return ClubRequest.findByIdAndCommunityAndPending(requestId, community).flatMap(request -> {
            if(request == null) {
              return Uni.createFrom().failure(new NotFoundException("Club's request Not found"));
            }

            request.approvalDate = Instant.now();
            request.rejected = true;
            return Panache.withTransaction(request::persist);
          });
        });
  }

  @Override
  public Uni<ClubRequest> clubRequest(String memberId, ClubRequestDto dto) {
    return clubRequest(memberId, dto.communityId(), dto);
  }

  private Uni<User> findByIdNonCommunity(String id) {
    return User.findByIdNonCommunityOrClub(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }

  private Uni<Boolean> checkIfRequestExist(String name, User community) {
    return ClubRequest.countByNameAndCommunity(name, community).map(count -> count > 0);
  }

  private Uni<Boolean> checkIfRequestExistById(String id, User community) {
    return ClubRequest.countByIdAndCommunityAndPending(id, community).map(count -> count > 0);
  }

  public Uni<ClubRequest> findClubRequest(String id) {
    return ClubRequest.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Club's request Not found"));
  }

  public Uni<ClubRequest> findClubRequestAndCommunity(String id, Community community) {
    return ClubRequest.findByIdAndCommunity(id, community).onItem().ifNull()
        .failWith(() -> new NotFoundException("Club's request Not found"));
  }

  public Uni<User> updateRequester(User community, String clubRequestId) {
    logger.debug("updateRequester: community: {}, clubRequestId: {}", community, clubRequestId);
    if(clubRequestId == null) return Uni.createFrom().nullItem();

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

  public Uni<User> saveClub(RegisterRes register, RegisterCommunityDto dto, User community, String clubRequestId) {
//    logger.debug("saveClub: persisting {} to database", register.name());

    return User.findById(register.id())
        .replaceIfNullWith(new User(register.id(), register.name(), register.email(), register.username()))
        .flatMap(club ->
            updateRequester(community, clubRequestId).flatMap(reqOrCom -> {
              logger.debug("saveClub: reqOrCom is: {}", reqOrCom);
              club.type = UserType.CLUB;
              club.owner = community;
              club.bio = dto.bio();
              club.enabled = register.enabled();

              // if there is no requester, make community owner(staff) as requester
              club.requester = reqOrCom != null? reqOrCom : community.owner;
              return Panache.withTransaction(club::persist);
            }));
  }

  @Override
  public Uni<Long> countCommunityClubs(String communityId) {
    return User.countCommunityClubs_(communityId, UserType.CLUB);
  }

  @Override
  public Uni<User> createClub(String communityId, RegisterCommunityDto dto) {
    return findByIdCommunityOrClub(communityId)
        .flatMap(community -> userManagerApi.registerClub(dto)
            .flatMap(res -> saveClub(res, dto, community, null)
                .flatMap(club -> addRequesterAsMemberToClub(club))));
  }
}

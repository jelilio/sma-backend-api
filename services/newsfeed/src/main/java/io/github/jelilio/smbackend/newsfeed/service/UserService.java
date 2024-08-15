package io.github.jelilio.smbackend.newsfeed.service;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.UserBioUpdateDto;
import io.github.jelilio.smbackend.common.dto.response.PhotoRes;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.dto.response.UserProfile;
import io.github.jelilio.smbackend.common.dto.response.UserUpdatedRes;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.ClubRequest;
import io.github.jelilio.smbackend.newsfeed.entity.Follow;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.projection.*;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface UserService {
  Uni<Paged<UserCommunity>> searchAll(String userId, String text, int size, int index);

  Uni<UserProfile> profile(String userId);

  Uni<User> findByIdOrNull(UUID id);

  Uni<UserProfile> profile(String userId, User loggedInUser);

  Uni<List<User>> findAll();

  Uni<Paged<User>> findAll(int size, int index);

  Uni<User> findById(String id);

  Uni<User> findByIdAndType(String id, UserType type);

  Uni<User> findByCommunity(String id);

  Uni<User> save(RegisterRes register, RegisterCommunityDto dto, User owner, UserType type);

  Uni<User> save(RegisterRes register);

  Uni<User> updateUserInfo(UserUpdatedRes register);

  Uni<Follow> follow(String userId, String otherUserId);

  Uni<Follow> requestForMembership(String userId, String otherUserId);

  Uni<Boolean> unfollow(String userId, String otherUserId);

  Uni<List<User>> followings(String userId);

  Uni<List<Follower>> allFollowers(String userId);

  Uni<List<Member>> allPendings(String userId);

  Uni<Paged<Following>> followings(String userId, int size, int index);

  Uni<Paged<Following>> followingsCommunitiesClubs(String userId, int size, int index);

  Uni<Paged<Following>> followingsCommunitiesOrClubs(String userId, UserType type, int size, int index);

  Uni<Paged<Following>> followings(String loggedInUserId, String userId, int size, int index);

  Uni<List<User>> followingCommunities(String userId);

  Uni<List<User>> followers(String userId);

  // for communities and clubs
  Uni<List<User>> members(String userId);

  Uni<Paged<Member>> pendings(String loggedInUserId, String userId, int size, int index);

  Uni<Paged<Follower>> members(String loggedInUserId, String userId, int size, int index);

  Uni<Paged<Community>> communities(String loggedInUserId, int size, int index);

  Uni<Paged<Community>> clubs(String loggedInUserId, int size, int index);

  Uni<Paged<Club>> clubs(String loggedInUserId, String communityId, int size, int index);

  // for communities and clubs
  Uni<List<Member>> members_(String userId);

  Uni<Paged<Follower>> followers(String userId, int size, int index);

  Uni<Paged<Member>> pendings(String userId, int size, int index);

  Uni<Paged<Follower>> followers(String loggedInUserId, String userId, int size, int index);

  Uni<List<Follower>> allFollowings(String userId);

  Uni<Paged<Member>> members(String userId, int size, int index);

  Uni<Paged<Member>> memberRequests(String communityId, int size, int index);

  // find all communities a member belongs to
//  Uni<List<Community>> communities_(String memberId);

//  Uni<Paged<Community>> communities(String memberId, int size, int index);

  Uni<Boolean> checkIfAlreadyAMember(User community, User requester);

  Uni<Paged<ClubRequest>> clubRequests(String communityId, int size, int index);

  Uni<Paged<ClubRequest>> clubAllRequests(String communityId, int size, int index);

  Uni<User> updateNameBio(User user, UserBioUpdateDto dto);

  Uni<User> updateAvatar(User user, PhotoRes dto);

  // fetch by type
  Uni<Paged<User>> findAllByType(UserType type, int size, int index);

  // fetch all communities
  Uni<Paged<User>> findAllCommunities(int size, int index);

  Uni<Boolean> approveVerification(String loggedInUsername, User user);

  Uni<Void> requestVerification(User user);

  Uni<User> disableOrEnableUser(String id, Boolean value);
}

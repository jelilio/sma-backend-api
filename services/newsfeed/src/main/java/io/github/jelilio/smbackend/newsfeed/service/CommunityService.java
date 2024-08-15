package io.github.jelilio.smbackend.newsfeed.service;

import io.github.jelilio.smbackend.common.dto.ClubRequestDto;
import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.newsfeed.entity.ClubRequest;
import io.github.jelilio.smbackend.newsfeed.entity.Follow;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.smallrye.mutiny.Uni;

public interface CommunityService {
  Uni<User> createCommunity(String loggedInUserId, RegisterCommunityDto dto);

  // for community and club
  Uni<Follow> acceptMembershipRequest(String loggedInCommunityId, String otherUserId);

  Uni<Follow> addMember(String userId, String otherUserId);

  Uni<Boolean> checkIfAlreadyAMember(User community, User requester);

  Uni<Boolean> cancelMembershipRequest(String communityId, String otherUserId);

  Uni<ClubRequest> clubRequest(String memberId, String communityId, ClubRequestDto dto);

  Uni<User> acceptClubRequest(String loggedInCommunityId, String requestId, RegisterCommunityDto dto);

  Uni<ClubRequest> rejectClubRequest(String loggedInCommunityId, String requestId);

  @Deprecated
  Uni<ClubRequest> clubRequest(String memberId, ClubRequestDto dto);

  Uni<Long> countCommunityClubs(String communityId);

  Uni<User> createClub(String communityId, RegisterCommunityDto dto);
}

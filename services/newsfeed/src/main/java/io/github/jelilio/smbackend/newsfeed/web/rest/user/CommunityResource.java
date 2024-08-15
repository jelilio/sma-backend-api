package io.github.jelilio.smbackend.newsfeed.web.rest.user;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.ClubRequest;
import io.github.jelilio.smbackend.newsfeed.entity.projection.Club;
import io.github.jelilio.smbackend.newsfeed.entity.projection.Member;
import io.github.jelilio.smbackend.newsfeed.service.CommunityService;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/user/community")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_USER_COM")
public class CommunityResource {
  private static final Logger logger = LoggerFactory.getLogger(CommunityResource.class);
  @Claim(USER_ID)
  String loggedInCommunityId;

  @Inject
  UserService userService;

  @Inject
  CommunityService communityService;

  @GET
  @Path("/members/all")
  public Uni<List<Member>> members() {
    logger.debug("fetch members of: community: {}", loggedInCommunityId);
    return userService.members_(loggedInCommunityId);
  }

  @GET
  @Path("/members")
  // fetch
  public Uni<Paged<Member>> members(@BeanParam PageRequest pageRequest) {
    logger.debug("fetch members of: community: {}", loggedInCommunityId);
    return userService.members(loggedInCommunityId, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("/members/add") // for community and club
  public Uni<Response> addMember(@RequestBody String otherUserId) {
    logger.debug("addMember: userCommId: {}, otherUserId: {}", loggedInCommunityId, otherUserId);
    return communityService.addMember(loggedInCommunityId, otherUserId)
        .onItem().transform(item -> Response.ok()
            .entity(item).build());
  }

  @GET
  @Path("/members/request")
  // fetch all pending requests
  public Uni<Paged<Member>> memberRequests(@BeanParam PageRequest pageRequest) {
    logger.debug("fetch member-requests of: community: {}", loggedInCommunityId);
    return userService.memberRequests(loggedInCommunityId, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("/members/accept-request") // for community and club
  public Uni<Response> acceptMembershipRequest(@RequestBody String otherUserId) {
    logger.debug("follow: userCommId: {}, otherUserId: {}", loggedInCommunityId, otherUserId);
    return communityService.acceptMembershipRequest(loggedInCommunityId, otherUserId)
        .onItem().transform(item -> Response.ok()
            .entity(item).build());
  }

  // both approved and pending
  @GET
  @Path("/clubs/requests/all")
  public Uni<Paged<ClubRequest>> clubAllRequests(@BeanParam PageRequest pageRequest) {
    logger.debug("fetch all clubRequests of: community: {}", loggedInCommunityId);
    return userService.clubAllRequests(loggedInCommunityId, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("/clubs/requests/{id}/accept") // for community and club
  public Uni<Response> acceptClubRequest(@PathParam("id") String requestId, @RequestBody @Valid RegisterCommunityDto dto) {
    logger.debug("acceptClubRequest: userCommId: {}, dto: {}", loggedInCommunityId, dto);
    return communityService.acceptClubRequest(loggedInCommunityId, requestId, dto)
        .onItem().transform(item -> Response.ok()
            .entity(item).build());
  }

  @POST
  @Path("/clubs/requests/{id}/reject") // for community and club
  public Uni<Response> rejectClubRequest(@PathParam("id") String requestId) {
    logger.debug("rejectClubRequest: userCommId: {}", loggedInCommunityId);
    return communityService.rejectClubRequest(loggedInCommunityId, requestId)
        .onItem().transform(item -> Response.ok()
            .entity(item).build());
  }

  @POST
  @Path("/clubs") // for community and club
  public Uni<Response> createClub(@RequestBody @Valid RegisterCommunityDto dto) {
    logger.debug("follow: userCommId: {}, dto: {}", loggedInCommunityId, dto);
    return communityService.createClub(loggedInCommunityId, dto)
        .onItem().transform(item -> Response.ok()
            .entity(item).build());
  }

  // only pending
  @GET
  @Path("/clubs/pending")
  public Uni<Paged<ClubRequest>> clubRequests(@BeanParam PageRequest pageRequest) {
    logger.debug("fetch pending clubRequests of: community: {}", loggedInCommunityId);
    return userService.clubRequests(loggedInCommunityId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/clubs/approved")
  public Uni<Paged<Club>> allCommunityClubs(@BeanParam PageRequest pageRequest) {
    return userService.clubs(loggedInCommunityId, loggedInCommunityId, pageRequest.size, pageRequest.page);
  }
}

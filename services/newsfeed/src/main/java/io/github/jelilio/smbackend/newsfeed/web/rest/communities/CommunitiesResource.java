package io.github.jelilio.smbackend.newsfeed.web.rest.communities;

import io.github.jelilio.smbackend.common.dto.ClubRequestDto;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.projection.Club;
import io.github.jelilio.smbackend.newsfeed.entity.projection.Community;
import io.github.jelilio.smbackend.newsfeed.entity.projection.Follower;
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

import java.net.URI;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/communities")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_USER")
public class CommunitiesResource {
  private static final Logger logger = LoggerFactory.getLogger(CommunitiesResource.class);

  @Claim(USER_ID)
  String loggedIdUserId;

  @Inject
  UserService userService;

  @Inject
  CommunityService communityService;

  @GET
  @Path("")
  public Uni<Paged<Community>> allCommunities(@BeanParam PageRequest pageRequest) {
    return userService.communities(loggedIdUserId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/{id}/members")
  public Uni<Paged<Follower>> members(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    logger.debug("members: userId: {}", userId);
    return userService.members(loggedIdUserId, userId, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("/{id}/request") // user request to be a member of a community
  public Uni<Response> requestForMembership(@PathParam("id") String communityId) {
    logger.debug("follow: userId: {}, otherUserId: {}", loggedIdUserId, communityId);
    return userService.requestForMembership(loggedIdUserId, communityId)
        .onItem().transform(item -> Response.ok()
            .entity(item).build());
  }

  @POST
  @Path("/{id}/clubs/request")
  public Uni<Response> requestForClubCreation(@PathParam("id") String communityId, @RequestBody @Valid ClubRequestDto dto) {
    logger.debug("member request for club creation: userId: {}, otherUserId: {}", loggedIdUserId, dto);
    return communityService.clubRequest(loggedIdUserId, communityId, dto).onItem()
        .transform(it -> Response.created(URI.create("/")).entity(it).build());
  }

  @GET
  @Path("/{id}/clubs")
  public Uni<Paged<Club>> allCommunityClubs(@PathParam("id") String communityId, @BeanParam PageRequest pageRequest) {
    return userService.clubs(loggedIdUserId, communityId, pageRequest.size, pageRequest.page);
  }
}

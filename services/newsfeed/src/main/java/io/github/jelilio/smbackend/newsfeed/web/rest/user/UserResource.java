package io.github.jelilio.smbackend.newsfeed.web.rest.user;

import io.github.jelilio.smbackend.common.dto.UserBioUpdateDto;
import io.github.jelilio.smbackend.common.dto.response.PhotoRes;
import io.github.jelilio.smbackend.common.dto.response.UserProfile;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.Notification;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.projection.*;
import io.github.jelilio.smbackend.newsfeed.service.NotificationService;
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
@Path("/api/user")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_USER")
public class UserResource {
  private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

  @Claim(USER_ID)
  String userId;

  @Inject
  UserService userService;

  @Inject
  NotificationService notificationService;

  @GET
  @Path("/profile")
  public Uni<UserProfile> profile() {
    logger.debug("profile: userId: {}", userId);
    return userService.profile(userId);
  }

  @GET
  @Path("/search")
  public Uni<Paged<UserCommunity>> allCommunities(@QueryParam("q") String text, @BeanParam PageRequest pageRequest) {
    return userService.searchAll(userId, text, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/followings")
  public Uni<Paged<Following>> followings(@BeanParam PageRequest pageRequest) {
    logger.debug("followings: userId: {}", userId);
    return userService.followings(userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/communities")
  public Uni<Paged<Following>> communities(@BeanParam PageRequest pageRequest) {
    logger.debug("communities: userId: {}", userId);
    return userService.followingsCommunitiesOrClubs(userId, UserType.COMMUNITY, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/clubs")
  public Uni<Paged<Following>> clubs(@BeanParam PageRequest pageRequest) {
    logger.debug("clubs: userId: {}", userId);
    return userService.followingsCommunitiesOrClubs(userId, UserType.CLUB, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/followers")
  public Uni<Paged<Follower>> followers(@BeanParam PageRequest pageRequest) {
    logger.debug("followers: userId: {}", userId);
    return userService.followers(userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/pendings")
  public Uni<Paged<Member>> pendings(@BeanParam PageRequest pageRequest) {
    logger.debug("pendings: userId: {}", userId);
    return userService.pendings(userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/members")
  public Uni<Paged<Member>> members(@BeanParam PageRequest pageRequest) {
    logger.debug("members: userId: {}", userId);
    return userService.members(userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/followings/all")
  public Uni<List<Follower>> followers() {
    logger.debug("all followers: userId: {}", userId);
    return userService.allFollowings(userId);
  }

  @PUT
  @Path("/name")
  public Uni<Response> updateName(@Valid @RequestBody UserBioUpdateDto dto) {
    Uni<User> userUni = userService.findById(userId);

    return userUni
        .flatMap(user -> userService.updateNameBio(user, dto)
            .onItem().transform(response -> Response.ok(response).build()));
  }

  @PUT
  @Path("/avatar")
  public Uni<Response> updateName(@Valid @RequestBody PhotoRes dto) {
    Uni<User> userUni = userService.findById(userId);

    return userUni
        .flatMap(user -> userService.updateAvatar(user, dto)
            .onItem().transform(response -> Response.ok(response).build()));
  }

  @PUT
  @Path("/request-verification")
  public Uni<Response> requestVerification() {
    Uni<User> userUni = userService.findById(userId);

    return userUni
        .flatMap(user -> userService.requestVerification(user)
            .onItem().transform(response -> Response.ok(response).build()));
  }

  @GET
  @Path("/notifications")
  public Uni<Paged<Notification>> getUserNotifications(@BeanParam PageRequest pageRequest) {
    return  userService.findById(userId).flatMap(user -> notificationService.findAll(user, pageRequest.size, pageRequest.page));
  }
}

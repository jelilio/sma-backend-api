package io.github.jelilio.smbackend.newsfeed.web.rest.users;

import io.github.jelilio.smbackend.common.dto.response.UserProfile;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.projection.*;
import io.github.jelilio.smbackend.newsfeed.service.CommunityService;
import io.github.jelilio.smbackend.newsfeed.service.PostService;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static io.github.jelilio.smbackend.common.utils.Constants.USER_USERNAME;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/users/{id}")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("newsfeed.ROLE_USER")
public class UsersResource {
  private static final Logger logger = LoggerFactory.getLogger(UsersResource.class);

  @Claim(USER_ID)
  String loggedIdUserId;

  @Claim(USER_USERNAME)
  String username;

  @Inject
  SecurityIdentity securityIdentity;

  @Inject
  UserService userService;

  @Inject
  PostService postService;

  @Inject
  CommunityService communityService;

  @GET
  @Path("/profile")
  public Uni<UserProfile> profile(@PathParam("id") String userId) {
    logger.debug("profile: userId: {}", userId);
    return userService.findById(loggedIdUserId)
        .flatMap(loggedInuser -> userService.profile(userId, loggedInuser));
  }

  @POST
  @Path("/follow")
  public Uni<Response> follow(@PathParam("id") String otherUserId) {
    logger.debug("follow: userId: {}, to follow ==> otherUserId: {}", loggedIdUserId, otherUserId);
    return userService.follow(loggedIdUserId, otherUserId)
        .onItem().transform(item -> Response.ok()
            .entity(Map.of("success", true)).build());
  }

  @POST
  @Path("/accept")
  public Uni<Response> accept(@PathParam("id") String otherUserId) {
    logger.debug("accept-request: userId: {}, to follow ==> otherUserId: {}", loggedIdUserId, otherUserId);
    return communityService.acceptMembershipRequest(loggedIdUserId, otherUserId)
        .onItem().transform(item -> Response.ok()
            .entity(Map.of("success", true)).build());
  }

  @POST
  @Path("/cancel")
  public Uni<Response> cancel(@PathParam("id") String otherUserId) {
    logger.debug("accept-request: userId: {}, to follow ==> otherUserId: {}", loggedIdUserId, otherUserId);
    return communityService.cancelMembershipRequest(loggedIdUserId, otherUserId)
        .onItem().transform(item -> Response.ok()
            .entity(Map.of("success", true)).build());
  }

  @POST
  @Path("/unfollow")
  public Uni<Response> unfollow(@PathParam("id") String otherUserId) {
    logger.debug("unfollow: userId: {}, to unfollow ==> otherUserId: {}", loggedIdUserId, otherUserId);
    return userService.unfollow(loggedIdUserId, otherUserId)
        .onItem().transform(item -> Response.ok()
            .entity(Map.of("success", true)).build());
  }

  @GET
  @Path("/followings")
  public Uni<Paged<Following>> followings(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    logger.debug("followings: userId: {}", userId);
    return userService.followings(loggedIdUserId, userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/followers")
  public Uni<Paged<Follower>> followers(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    logger.debug("followers: userId: {}", userId);
    return userService.followers(loggedIdUserId, userId, pageRequest.size, pageRequest.page);
  }

  // for community and club
  @GET
  @Path("/pendings")
  public Uni<Paged<Member>> pendings(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    logger.debug("pendings: userId: {}", userId);
    return userService.pendings(loggedIdUserId, userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/members")
  public Uni<Paged<Follower>> members(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    logger.debug("members: userId: {}", userId);
    return userService.members(loggedIdUserId, userId, pageRequest.size, pageRequest.page);
  }
  //

  @GET
  @Path("/posts")
  @Operation(summary = "Authenticated user fetch a paginated list of user's posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Paged<PostPro>> get(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    return postService.findOtherUserPosts(loggedIdUserId, userId, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/posts/{postId}")
  @Operation(summary = "Authenticated user fetch a paginated list of his posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
  public Uni<Response> get(@PathParam("id") String ownerId, @PathParam("postId") String postId) {
    return postService.findPostById(loggedIdUserId, ownerId, postId).onItem().transform(item -> Response.ok().entity(item).build());
  }

  @POST
  @RolesAllowed("ROLE_ADMIN")
  @Path("/approve-verification")
  public Uni<Response> approveVerification(@PathParam("id") String id) {
    Uni<User> userUni = userService.findById(id);

    return userUni
        .flatMap(user -> userService.approveVerification(username, user)
            .onItem().transform(response -> Response.ok(response).build()));
  }
}

package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.users;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.users.NfUsersProxy;
import io.github.jelilio.smbackend.common.dto.response.MemberRes;
import io.github.jelilio.smbackend.common.dto.response.PostRes;
import io.github.jelilio.smbackend.common.dto.response.FollowRes;
import io.github.jelilio.smbackend.common.dto.response.UserProfile;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.PostProRes;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/users/{id}")
public class NfUsersApi {
  private static final Logger logger = LoggerFactory.getLogger(NfUsersApi.class);

  @Inject
  @RestClient
  NfUsersProxy nfUsersProxy;

  @GET
  @Path("/profile")
  public Uni<UserProfile> profile(@PathParam("id") String id) {
    logger.debug("profile: pageRequest: {}", id);
    return nfUsersProxy.profile(id);
  }

  @POST
  @Path("/follow")
  public Uni<Response> follow(@PathParam("id") String otherUserId) {
    return nfUsersProxy.follow(otherUserId);
  }

  @POST
  @Path("/unfollow")
  public Uni<Response> unfollow(@PathParam("id") String otherUserId) {
    return nfUsersProxy.unfollow(otherUserId);
  }

  @POST
  @Path("/accept")
  public Uni<Response> accept(@PathParam("id") String otherUserId) {
    return nfUsersProxy.accept(otherUserId);
  }

  @POST
  @Path("/cancel")
  public Uni<Response> cancel(@PathParam("id") String otherUserId) {
    return nfUsersProxy.cancel(otherUserId);
  }

  @GET
  @Path("/followings")
  public Uni<Paged<FollowRes>> followings(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("followings: pageRequest: {}", pageRequest);
    return nfUsersProxy.followings(id, pageRequest);
  }

  @GET
  @Path("/followers")
  public Uni<Paged<FollowRes>> followers(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("followers: pageRequest: {}", pageRequest);
    return nfUsersProxy.followers(id, pageRequest);
  }

  @GET
  @Path("/pendings")
  public Uni<Paged<MemberRes>> pendings(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("pendings: pageRequest: {}", pageRequest);
    return nfUsersProxy.pendings(id, pageRequest);
  }

  @GET
  @Path("/members")
  public Uni<Paged<FollowRes>> members(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("members: pageRequest: {}", pageRequest);
    return nfUsersProxy.members(id, pageRequest);
  }

  @GET
  @Path("/posts")
  @Authenticated
  @Operation(summary = "Authenticated user fetch a paginated list of user's posts")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Paged<PostProRes>> get(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    return nfUsersProxy.posts(id, pageRequest);
  }

  @GET
  @Path("/posts/{postId}")
  @Operation(summary = "Fetch a post")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = PostRes.class)))
  public Uni<Response> get(@PathParam("id") String userId, @PathParam("postId") String postId) {
    return nfUsersProxy.get(userId, postId);
  }
}

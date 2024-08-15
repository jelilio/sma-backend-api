package io.github.jelilio.smbackend.apigateway.client.newsfeed.users;

import io.github.jelilio.smbackend.common.dto.response.FollowRes;
import io.github.jelilio.smbackend.common.dto.response.MemberRes;
import io.github.jelilio.smbackend.common.dto.response.UserProfile;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.PostProRes;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/users/{id}")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfUsersProxy {
  @GET
  @Path("/profile")
  Uni<UserProfile> profile(@PathParam("id") String userId);

  @POST
  @Path("/follow")
  Uni<Response> follow(@PathParam("id") String otherUserId);

  @POST
  @Path("/unfollow")
  Uni<Response> unfollow(@PathParam("id") String otherUserId);

  @POST
  @Path("/accept")
  Uni<Response> accept(@PathParam("id") String otherUserId);

  @POST
  @Path("/cancel")
  Uni<Response> cancel(@PathParam("id") String otherUserId);

  @GET
  @Path("/followings")
  Uni<Paged<FollowRes>> followings(@PathParam("id") String userId, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/followers")
  Uni<Paged<FollowRes>> followers(@PathParam("id") String userId, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/pendings")
  Uni<Paged<MemberRes>> pendings(@PathParam("id") String userId, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/members")
  Uni<Paged<FollowRes>> members(@PathParam("id") String userId, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/posts")
  Uni<Paged<PostProRes>> posts(@PathParam("id") String userId, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/posts/{postId}")
  Uni<Response> get(@PathParam("id") String userId, @PathParam("postId") String postId);

  @POST
  @Path("/approve-verification")
  Uni<Response> approveVerification(@PathParam("id") String id);
}

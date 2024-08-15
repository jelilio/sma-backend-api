package io.github.jelilio.smbackend.apigateway.client.newsfeed.user;

import io.github.jelilio.smbackend.common.dto.UserBioUpdateDto;
import io.github.jelilio.smbackend.common.dto.response.*;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.NotificationRes;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/user")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfUserProxy {

  @GET
  @Path("/profile")
  Uni<UserProfile> profile();

  @GET
  @Path("/search")
  Uni<Paged<UserCommunityRes>> searchAll(@QueryParam("q") String query, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/followings")
  Uni<Paged<FollowRes>> followings(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/communities")
  Uni<Paged<FollowComClubRes>> communities(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/clubs")
  Uni<Paged<FollowComClubRes>> clubs(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/followers")
  Uni<Paged<FollowRes>> followers(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/pendings")
  Uni<Paged<MemberRes>> pendings(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/members")
  Uni<Paged<MemberRes>> members(@BeanParam PageRequest pageRequest);

  @PUT
  @Path("/name")
  Uni<Response> updateName(@Valid @RequestBody UserBioUpdateDto dto);

  @PUT
  @Path("/avatar")
  Uni<Response> updateAvatar(@Valid @RequestBody PhotoRes dto);

  @PUT
  @Path("/request-verification")
  Uni<Response> requestVerification();

  @GET
  @Path("/notifications")
  Uni<Paged<NotificationRes>> notifications(@BeanParam PageRequest pageRequest);

}

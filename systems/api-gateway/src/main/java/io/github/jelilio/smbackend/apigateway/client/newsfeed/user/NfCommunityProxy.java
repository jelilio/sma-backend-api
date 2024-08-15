package io.github.jelilio.smbackend.apigateway.client.newsfeed.user;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.response.ClubRequestRes;
import io.github.jelilio.smbackend.common.dto.response.ClubRes;
import io.github.jelilio.smbackend.common.dto.response.MemberRes;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/user/community/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfCommunityProxy {
  @GET
  @Path("/members/all")
  Uni<List<MemberRes>> fetchAll();

  @GET
  @Path("/members")
  Uni<Paged<MemberRes>> fetchAll(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/members/request")
  Uni<Paged<MemberRes>> memberRequests(@BeanParam PageRequest pageRequest);

  @POST
  @Path("/members/add")
  Uni<Response> addMember(@RequestBody String otherUserId);

  @POST
  @Path("/members/accept-request")
  Uni<Response> acceptMembershipRequest(@RequestBody String otherUserId);

  @GET
  @Path("/clubs/requests/all")
  Uni<Paged<ClubRequestRes>> clubAllRequests(@BeanParam PageRequest pageRequest);

  @POST
  @Path("/clubs/requests/{id}/accept")
  Uni<Response> acceptClubRequest(@PathParam("id") String requestId, @RequestBody @Valid RegisterCommunityDto dto);

  @POST
  @Path("/clubs/requests/{id}/reject")
  Uni<Response> rejectClubRequest(@PathParam("id") String requestId);

  @POST
  @Path("/clubs") // for community and club
  Uni<Response> createClub(@RequestBody @Valid RegisterCommunityDto dto);

  @GET
  @Path("/clubs/pending")
  Uni<Paged<ClubRequestRes>> clubRequests(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/clubs/approved")
  Uni<Paged<ClubRes>> fetchAllClubs(@BeanParam PageRequest pageRequest);
}

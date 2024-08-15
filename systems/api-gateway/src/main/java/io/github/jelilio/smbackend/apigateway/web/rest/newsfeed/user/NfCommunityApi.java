package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.user;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.user.NfCommunityProxy;
import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.response.ClubRequestRes;
import io.github.jelilio.smbackend.common.dto.response.ClubRes;
import io.github.jelilio.smbackend.common.dto.response.MemberRes;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/user/community")
public class NfCommunityApi {
  @Inject
  @RestClient
  NfCommunityProxy nfCommunityProxy;

  @GET
  @Path("/members/all")
  public Uni<List<MemberRes>> members() {
    return nfCommunityProxy.fetchAll();
  }

  @GET
  @Path("/members")
  public Uni<Paged<MemberRes>> members(@BeanParam PageRequest pageRequest) {
    return nfCommunityProxy.fetchAll(pageRequest);
  }

  @POST
  @Path("/members/add") // for community and club
  public Uni<Response> addMember(@RequestBody String otherUserId) {
    return nfCommunityProxy.addMember(otherUserId);
  }

  @GET
  @Path("/members/request")
  // fetch all pending requests
  public Uni<Paged<MemberRes>> memberRequests(@BeanParam PageRequest pageRequest) {
    return nfCommunityProxy.memberRequests(pageRequest);
  }

  @POST
  @Path("/members/accept-request") // for community and club
  public Uni<Response> acceptMembershipRequest(@RequestBody String otherUserId) {
    return nfCommunityProxy.acceptMembershipRequest(otherUserId);
  }

  @GET
  @Path("/clubs/requests/all")
  public Uni<Paged<ClubRequestRes>> clubAllRequests(@BeanParam PageRequest pageRequest) {
    return nfCommunityProxy.clubAllRequests(pageRequest);
  }

  @POST
  @Path("/clubs/requests/{id}/accept")// for community and club
  public Uni<Response> acceptClubRequest(@PathParam("id") String requestId, @RequestBody @Valid RegisterCommunityDto dto) {
    return nfCommunityProxy.acceptClubRequest(requestId, dto);
  }

  @POST
  @Path("/clubs/requests/{id}/reject")// for community and club
  public Uni<Response> rejectClubRequest(@PathParam("id") String requestId) {
    return nfCommunityProxy.rejectClubRequest(requestId);
  }

  @POST
  @Path("/clubs") // for community and club
  public Uni<Response> createClub(@RequestBody @Valid RegisterCommunityDto dto) {
    return nfCommunityProxy.createClub(dto);
  }

  @GET
  @Path("/clubs/pending")
  public Uni<Paged<ClubRequestRes>> clubRequests(@BeanParam PageRequest pageRequest) {
    return nfCommunityProxy.clubRequests(pageRequest);
  }

  @GET
  @Path("/clubs/approved")
  public Uni<Paged<ClubRes>> clubs(@BeanParam PageRequest pageRequest) {
    return nfCommunityProxy.fetchAllClubs(pageRequest);
  }
}

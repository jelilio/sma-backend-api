package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.communities;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.communties.NfCommunitiesProxy;
import io.github.jelilio.smbackend.common.dto.ClubRequestDto;
import io.github.jelilio.smbackend.common.dto.response.CommunityRes;
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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/communities")
public class NfCommunitiesApi {

  @Inject
  @RestClient
  NfCommunitiesProxy nfCommunitiesProxy;

  @GET
  @Path("")
  public Uni<Paged<CommunityRes>> allCommunities(@BeanParam PageRequest pageRequest) {
    return nfCommunitiesProxy.allCommunities(pageRequest);
  }

  @POST
  @Path("/{id}/request")
  public Uni<Response> requestForMembership(@PathParam("id") String communityId) {
    return nfCommunitiesProxy.requestForMembership(communityId);
  }

  @POST
  @Path("/{id}/clubs/request")
  public Uni<Response> requestForClubCreation(@PathParam("id") String communityId, @RequestBody @Valid ClubRequestDto dto) {
    return nfCommunitiesProxy.requestForClubCreation(communityId, dto);
  }

  @GET
  @Path("/{id}/clubs")
  public Uni<Paged<CommunityRes>> allCommunityClubs(@PathParam("id") String communityId,@BeanParam PageRequest pageRequest) {
    return nfCommunitiesProxy.allCommunityClubs(communityId, pageRequest);
  }
}

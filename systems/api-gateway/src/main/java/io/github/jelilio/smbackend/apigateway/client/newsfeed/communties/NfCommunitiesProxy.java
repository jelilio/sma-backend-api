package io.github.jelilio.smbackend.apigateway.client.newsfeed.communties;

import io.github.jelilio.smbackend.common.dto.ClubRequestDto;
import io.github.jelilio.smbackend.common.dto.response.CommunityRes;
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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/communities")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfCommunitiesProxy {
  @GET
  @Path("")
  Uni<Paged<CommunityRes>> allCommunities(@BeanParam PageRequest pageRequest);

  @POST
  @Path("/{id}/request")
  Uni<Response> requestForMembership(@PathParam("id") String communityId);

  @POST
  @Path("/{id}/clubs/request")
  Uni<Response> requestForClubCreation(@PathParam("id") String communityId, @RequestBody @Valid ClubRequestDto dto);

  @GET
  @Path("/{id}/clubs")
  Uni<Paged<CommunityRes>> allCommunityClubs(@PathParam("id") String communityId, @BeanParam PageRequest pageRequest);
}

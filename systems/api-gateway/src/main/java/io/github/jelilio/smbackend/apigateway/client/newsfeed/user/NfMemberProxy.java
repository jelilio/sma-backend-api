package io.github.jelilio.smbackend.apigateway.client.newsfeed.user;

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

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/user/member22/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfMemberProxy {
  @GET
  @Path("/communities/all")
  Uni<List<CommunityRes>> fetchAll();

  @GET
  @Path("/communities")
  Uni<Paged<CommunityRes>> fetchAll(@BeanParam PageRequest pageRequest);

  @POST
  @Path("/request")
  Uni<Response> requestForMembership(@RequestBody String userCommId);

  @POST
  @Path("/club/request")
  Uni<Response> requestForClubCreation(@RequestBody @Valid ClubRequestDto dto);
}

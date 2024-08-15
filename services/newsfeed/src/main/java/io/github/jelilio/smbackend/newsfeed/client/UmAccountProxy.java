package io.github.jelilio.smbackend.newsfeed.client;

import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.RegisterDto;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "usermanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface UmAccountProxy {
  @POST
  @Path("/account/register")
  Uni<RegisterRes> register(@Valid @RequestBody RegisterDto dto);

  @POST
  @Path("/staff/account/register-community")
  Uni<RegisterRes> registerCommunity(@Valid @RequestBody RegisterCommunityDto dto);

  @POST
  @Path("/community/account/register-club")
  Uni<RegisterRes> registerClub(@Valid @RequestBody RegisterCommunityDto dto);
}

package io.github.jelilio.smbackend.apigateway.web.rest.usermanager.staff;

import io.github.jelilio.smbackend.apigateway.client.usermanager.staff.UmAccountProxy;
import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/usermanager/api")
public class UmAccountApi {
  @Inject
  @RestClient
  UmAccountProxy userManagerApi;

  @POST
  @Path("/staff/account/register-community")
  public Uni<RegisterRes> register(@Valid @RequestBody RegisterCommunityDto dto) {
    return userManagerApi.registerCommunity(dto);
  }
}

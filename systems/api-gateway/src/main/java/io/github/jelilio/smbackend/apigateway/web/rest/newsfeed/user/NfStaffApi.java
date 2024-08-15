package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.user;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.user.NfStaffProxy;
import io.github.jelilio.smbackend.common.dto.RegisterCommunityDto;
import io.github.jelilio.smbackend.common.dto.RegisterDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/user/staff")
public class NfStaffApi {

  @Inject
  @RestClient
  NfStaffProxy nfStaffProxy;

  @POST
  @Path("/community") // create community
  public Uni<Response> register(@Valid @RequestBody RegisterCommunityDto dto) {
    return nfStaffProxy.registerCommunity(dto);
  }
}

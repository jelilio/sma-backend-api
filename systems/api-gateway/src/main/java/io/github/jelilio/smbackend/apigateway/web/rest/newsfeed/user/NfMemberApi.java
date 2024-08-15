package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.user;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.user.NfMemberProxy;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/user/member22")
public class NfMemberApi {
  private static final Logger logger = LoggerFactory.getLogger(NfMemberApi.class);

  @Inject
  @RestClient
  NfMemberProxy nfMemberProxy;

  @GET
  @Path("/communities/all")
  public Uni<List<CommunityRes>> communities() {
    return nfMemberProxy.fetchAll();
  }

  @GET
  @Path("/communities")
  public Uni<Paged<CommunityRes>> communities(@BeanParam PageRequest pageRequest) {
    return nfMemberProxy.fetchAll(pageRequest);
  }

  @POST
  @Path("/request") // user request to be a member of a community
  public Uni<Response> requestForMembership(@RequestBody String userCommId) {
    return nfMemberProxy.requestForMembership(userCommId);
  }

  @POST
  @Path("/club/request")
  public Uni<Response> requestForClubCreation(@RequestBody @Valid ClubRequestDto dto) {
    return nfMemberProxy.requestForClubCreation(dto);
  }
}

package io.github.jelilio.smbackend.apigateway.web.rest.custom;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.admin.NfAdminUsersProxy;
import io.github.jelilio.smbackend.apigateway.client.newsfeed.user.NfUserProxy;
import io.github.jelilio.smbackend.apigateway.client.newsfeed.users.NfUsersProxy;
import io.github.jelilio.smbackend.apigateway.client.usermanager.UmUserProxy;
import io.github.jelilio.smbackend.apigateway.client.usermanager.admin.UmUsersProxy;
import io.github.jelilio.smbackend.common.dto.UserBioUpdateDto;
import io.github.jelilio.smbackend.common.dto.response.EnableUserReq;
import io.github.jelilio.smbackend.common.dto.response.UserRes;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/custom/api")
public class CustomApi {
  @Inject
  @RestClient
  NfUserProxy nfUserProxy;

  @Inject
  @RestClient
  UmUserProxy umUserProxy;

  @Inject
  @RestClient
  UmUsersProxy usersProxy;

  @Inject
  @RestClient
  NfUsersProxy nfUsersProxy;

  @Inject
  @RestClient
  UmUserProxy userProxy;

  @Inject
  @RestClient
  NfAdminUsersProxy nfAdminUsersProxy;


  @ConfigProperty(name = "app.username.blacklist")
  String[] usernameBlacklist;

  @PUT
  @Path("/name")
  public Uni<Response> updateName(@Valid @RequestBody UserBioUpdateDto dto) {
    if(List.of(usernameBlacklist).contains(dto.username()))
      return Uni.createFrom().failure(new AlreadyExistException(String.format("Username: %s, already in used", dto.username())));

    return umUserProxy.updateName(dto).flatMap(response -> {
      return nfUserProxy.updateName(dto);
    });
  }

  @POST
  @Path("/{userId}/approve-verification")
  public Uni<Response> approveVerification(@PathParam("userId") String id) {
    return usersProxy.approveVerification(id)
        .flatMap(oidcId -> nfUsersProxy.approveVerification(oidcId));
  }

  @PUT
  @Path("/{id}/enable")
  @Operation(summary = "Enable/disable a user")
  @APIResponse(responseCode = "200", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<Response> enableOrDisable(@PathParam("id") String id, @Valid @RequestBody EnableUserReq enableUser) {
    return usersProxy.enableOrDisable(id, enableUser)
        .flatMap(userRes -> nfAdminUsersProxy.enableOrDisable(userRes.oidcId(), new EnableUserReq(userRes.enabled())));
  }

  @PUT
  @Path("/request-verification")
  public Uni<Response> requestVerification() {
    return userProxy.requestVerification()
        .flatMap(__ -> nfUserProxy.requestVerification());
  }
}

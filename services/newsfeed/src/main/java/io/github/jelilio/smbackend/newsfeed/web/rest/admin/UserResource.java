package io.github.jelilio.smbackend.newsfeed.web.rest.admin;

import io.github.jelilio.smbackend.common.dto.response.EnableUserReq;
import io.github.jelilio.smbackend.common.dto.response.UserRes;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.Violation;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.github.jelilio.smbackend.newsfeed.service.ViolationService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/admin/users")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("ROLE_ADMIN")
public class UserResource {
  @Inject
  UserService userService;

  @Inject
  ViolationService violationService;

  @GET
  @Path("")
  @Operation(summary = "Authorised user fetch a paginated list of his users")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<Paged<User>> get(@BeanParam PageRequest pageRequest) {
    return userService.findAll(pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Authorised user fetch a user")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<Response> get(@PathParam("id") String id) {
    return userService.findById(id).onItem().transform(item -> Response.ok().entity(item).build());
  }

  @GET
  @Path("/{id}/violations")
  @Operation(summary = "Authorised user fetch a paginated list of user's violations")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<Paged<Violation>> getUserViolations(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    return violationService.findAll(userId, pageRequest.size, pageRequest.page);
  }

  @PUT
  @Path("/{id}/enable")
  @Operation(summary = "Enable/disable a user")
  @APIResponse(responseCode = "200", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<Response> enableOrDisable(@PathParam("id") String id, @Valid @RequestBody EnableUserReq enableUser) {

    Uni<User> uni = userService.disableOrEnableUser(id, enableUser.enabled());

    return uni.map(inserted -> Response
        .ok()
        .entity(inserted)
        .build());
  }
}

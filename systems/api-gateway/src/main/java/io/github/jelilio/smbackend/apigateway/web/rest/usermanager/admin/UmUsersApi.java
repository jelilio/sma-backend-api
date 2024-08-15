package io.github.jelilio.smbackend.apigateway.web.rest.usermanager.admin;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.admin.NfAdminUsersProxy;
import io.github.jelilio.smbackend.apigateway.client.usermanager.admin.UmUsersProxy;
import io.github.jelilio.smbackend.common.dto.FileDto;
import io.github.jelilio.smbackend.common.dto.UserReq;
import io.github.jelilio.smbackend.common.dto.response.EnableUserReq;
import io.github.jelilio.smbackend.common.dto.response.UserRes;
import io.github.jelilio.smbackend.common.entity.enumeration.UserQueryType;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.ViolationRes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/usermanager/api/admin/users")
public class UmUsersApi {
  @Inject
  @RestClient
  UmUsersProxy usersProxy;

  @Inject
  @RestClient
  NfAdminUsersProxy nfAdminUsersProxy;

  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch a user by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<UserRes> get(@PathParam("id") String id) {
    return usersProxy.getById(id);
  }

  @GET
  @Path("/{id}/violations")
  public Uni<Paged<ViolationRes>> fetchViolations(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    return usersProxy.getById(userId).flatMap(userRes -> nfAdminUsersProxy.fetchViolations(userRes.oidcId(), pageRequest));
  }

  @GET
  @Path("")
  public Uni<Paged<UserRes>> findUsers(@RestQuery("type") UserType type, @BeanParam PageRequest pageRequest) {
    return usersProxy.findUsers(type, pageRequest);
  }

  @GET
  @Path("/all")
  public Uni<List<UserRes>> findAllUsers(@RestQuery("type") UserType type, @RestQuery("queryType") UserQueryType queryType) {
    return usersProxy.findAllUsers(type, queryType);
  }

  @POST
  @Path("")
  @Operation(summary = "Create a user")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<Response> create(@NotNull @RestQuery("type") UserType type, @Valid @RequestBody UserReq dto) {
    return usersProxy.create(type, dto);
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Update a user")
  @APIResponse(responseCode = "200", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<Response> update(@PathParam("id") String id, @NotNull @QueryParam("type") UserType type, @Valid @RequestBody UserReq dto) {
    return usersProxy.update(id, type, dto);
  }

  @PUT
  @Path("/{id}/enable")
  @Operation(summary = "Enable/disable a user")
  @APIResponse(responseCode = "200", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<UserRes> enableOrDisable(@PathParam("id") String id, @Valid @RequestBody EnableUserReq enableUser) {
    return usersProxy.enableOrDisable(id, enableUser);
  }


  @POST
  @Path("/batch-load")
  @Produces(APPLICATION_JSON)
  @Consumes(MULTIPART_FORM_DATA)
  public Uni<Response> batchLoad(@Valid @BeanParam FileDto dto) {
    return usersProxy.batchLoad(dto);
  }
}

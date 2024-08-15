package io.github.jelilio.smbackend.usermanager.web.rest.admin;

import io.github.jelilio.smbackend.common.dto.FileDto;
import io.github.jelilio.smbackend.common.dto.UserReq;
import io.github.jelilio.smbackend.common.dto.response.EnableUserReq;
import io.github.jelilio.smbackend.common.dto.response.UserRes;
import io.github.jelilio.smbackend.common.entity.enumeration.DateQueryType;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.entity.User;
import io.github.jelilio.smbackend.usermanager.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static io.github.jelilio.smbackend.common.utils.Constants.USER_USERNAME;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@WithSession
@RequestScoped
@Path("/api/admin/users")
@Produces(APPLICATION_JSON)
@RolesAllowed("ROLE_ADMIN")
public class UsersResource {
  private static final Logger logger = LoggerFactory.getLogger(UsersResource.class);

  @Claim(USER_ID)
  String subject;

  @Claim(USER_USERNAME)
  String username;

  @Inject
  UserService userService;

  @GET
  @Path("/all")
  @Operation(summary = "Authorised user fetch a list of users")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<List<User>> getAll(@RestQuery("type") UserType type, @RestQuery("queryType") DateQueryType queryType) {
    if(type == null) return userService.findAllByQueryType(queryType);

    return userService.findAllByTypeAndQueryType(type, queryType);
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch a user by oidc-id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<Response> get(@PathParam("id") String id) {
    return userService.findById(id).onItem().transform(item -> Response.ok().entity(item).build());
  }

  @GET
  @Path("")
  @Operation(summary = "Authorised user fetch a paginated list of his users")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<Paged<User>> get(@RestQuery("type") UserType type, @BeanParam PageRequest pageRequest) {
    if(type == null) return userService.findAll(pageRequest.size, pageRequest.page);

    return userService.findAllByType(type, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  @Operation(summary = "Create a user")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<Response> create(@NotNull @RestQuery("type") UserType type, @Valid @RequestBody UserReq dto) {
    logger.debug("create student: type: {}, dto: {}", type, dto);

    Uni<User> uni = switch (type) {
      case STUDENT, ALUMNI -> userService.createStudent(dto, type);
      case STAFF -> userService.createStaff(dto, type);
      default -> Uni.createFrom().failure(new NotFoundException("Resources not found"));
    };

    return uni.map(inserted -> Response
            .created(URI.create("/api/users/" + inserted.id))
            .entity(inserted)
            .build());
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Update a user")
  @APIResponse(responseCode = "200", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<Response> update(@PathParam("id") String id, @NotNull @QueryParam("type") UserType type, @Valid @RequestBody UserReq dto) {
    logger.debug("update student: id: {}, type: {}, dto: {}", id, type, dto);

    Uni<User> uni = switch (type) {
      case STUDENT, ALUMNI -> userService.updateStudent(id, dto);
      case STAFF -> userService.updateStaff(id, dto);
      default -> Uni.createFrom().failure(new NotFoundException("Resources not found"));
    };

    return uni.map(inserted -> Response
            .ok()
            .entity(inserted)
            .build());
  }

  @PUT
  @Path("/{id}/enable")
  @Operation(summary = "Enable/disable a user")
  @APIResponse(responseCode = "200", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserRes.class)))
  public Uni<Response> enableOrDisable(@PathParam("id") String id, @Valid @RequestBody EnableUserReq enableUser) {
    logger.debug("update user: id: {}, enableUser: {}", id, enableUser);

    Uni<User> uni = userService.disableOrEnableUser(id, enableUser.enabled());

    return uni.map(inserted -> Response
        .ok()
        .entity(inserted)
        .build());
  }

  @POST
  @Path("/batch-load")
  @Produces(APPLICATION_JSON)
  @Consumes(MULTIPART_FORM_DATA)
  public Uni<Response> batchLoad(@Valid @BeanParam FileDto dto) {
    return userService.upload(dto)
        .onItem().transform(inserted -> Response.ok().build());
  }

  @POST
  @Path("/{id}/approve-verification")
  public Uni<Response> approveVerification(@PathParam("id") String id) {
    Uni<User> userUni = userService.findById(id);

    return userUni
        .flatMap(user -> userService.approveVerification(username, user)
            .onItem().transform(oidcId -> Response.ok(oidcId).build()));
  }
}

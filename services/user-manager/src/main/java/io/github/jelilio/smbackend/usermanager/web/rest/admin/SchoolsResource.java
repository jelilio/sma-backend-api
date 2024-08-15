package io.github.jelilio.smbackend.usermanager.web.rest.admin;

import io.github.jelilio.smbackend.common.dto.SchoolReq;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.github.jelilio.smbackend.usermanager.service.SchoolService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@WithSession
@RequestScoped
@RolesAllowed("ROLE_ADMIN")
@Path("/api/admin/schools")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class SchoolsResource {
  private static final Logger logger = LoggerFactory.getLogger(SchoolsResource.class);

  @Inject
  SchoolService schoolService;

  @GET
  @Path("/all")
  @Operation(summary = "Fetch a list of schools")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = School.class)))
  public Uni<List<School>> getAll() {
    return schoolService.findAll();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch an school by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = School.class)))
  public Uni<Response> getById(@PathParam("id") String id) {
    return schoolService.findById(id).onItem().transform(item -> Response.ok().entity(item).build());
  }

  @GET
  @Path("")
  @Operation(summary = "Fetch a paginated list of schools")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = School.class)))
  public Uni<Paged<School>> getAll(@BeanParam PageRequest pageRequest) {
    return schoolService.findAll(pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  @Operation(summary = "Create an school")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = School.class)))
  public Uni<Response> create(@Valid @RequestBody SchoolReq dto) {
    logger.debug("create school: {}", dto);

    return schoolService.create(dto)
        .map(inserted -> Response
            .created(URI.create("/api/schools/" + inserted.id))
            .entity(inserted)
            .build());
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Create an school")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = School.class)))
  public Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody SchoolReq dto) {
    logger.debug("create school: {}", dto);

    return schoolService.update(id, dto)
        .map(inserted -> Response
            .ok()
            .entity(inserted)
            .build());
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete an school by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("id") String id) {
    logger.info("about to delete: {}", id);
    return schoolService.delete(id).onItem().transform(item -> Response.ok().build());
  }
}

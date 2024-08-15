package io.github.jelilio.smbackend.usermanager.web.rest.admin;

import io.github.jelilio.smbackend.common.dto.InstitutionReq;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.service.InstitutionService;
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
@Path("/api/admin/institutions")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class InstitutionsResource {
  private static final Logger logger = LoggerFactory.getLogger(InstitutionsResource.class);

  @Inject
  InstitutionService institutionService;

  @GET
  @Path("/all")
  @Operation(summary = "Fetch a list of institutions")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Institution.class)))
  public Uni<List<Institution>> getAll() {
    return institutionService.findAll();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch an institution by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Institution.class)))
  public Uni<Response> getById(@PathParam("id") String id) {
    return institutionService.findById(id).onItem().transform(item -> Response.ok().entity(item).build());
  }

  @GET
  @Path("")
  @Operation(summary = "Fetch a paginated list of institutions")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Institution.class)))
  public Uni<Paged<Institution>> getAll(@BeanParam PageRequest pageRequest) {
    return institutionService.findAll(pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  @Operation(summary = "Create an institution")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Institution.class)))
  public Uni<Response> create(@Valid @RequestBody InstitutionReq dto) {
    logger.debug("create institution: {}", dto);

    return institutionService.create(dto)
        .map(inserted -> Response
            .created(URI.create("/api/institutions/" + inserted.id))
            .entity(inserted)
            .build());
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Create an institution")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Institution.class)))
  public Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody InstitutionReq dto) {
    logger.debug("create institution: {}", dto);

    return institutionService.update(id, dto)
        .map(inserted -> Response
            .ok()
            .entity(inserted)
            .build());
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete an institution by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("id") String id) {
    logger.info("about to delete: {}", id);
    return institutionService.delete(id).onItem().transform(item -> Response.ok().build());
  }
}

package io.github.jelilio.smbackend.newsfeed.web.rest.admin;

import io.github.jelilio.smbackend.common.entity.enumeration.DateQueryType;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.newsfeed.entity.Violation;
import io.github.jelilio.smbackend.newsfeed.service.ViolationService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.RestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed("ROLE_ADMIN")
@Path("/api/admin/violations")
public class ViolationResource {
  private static final Logger logger = LoggerFactory.getLogger(ViolationResource.class);

  @Inject
  ViolationService violationService;

  @GET
  @Path("/all")
  @Operation(summary = "Authorised user fetch a list of violations")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Violation.class)))
  public Uni<List<Violation>> getAll(@RestQuery("queryType") DateQueryType queryType) {
    return violationService.findAllByQueryType(queryType);
  }

  @GET
  @Path("/report")
  @Operation(summary = "Authorised user fetch a list of violations")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Violation.class)))
  public Uni<Paged<Violation>> getReport(@RestQuery("queryType") DateQueryType queryType, @BeanParam PageRequest pageRequest) {
    return violationService.findAllByQueryType(queryType, pageRequest.size, pageRequest.page);
  }

  @GET
  @Path("")
  @Operation(summary = "Authorised user fetch a paginated list of all violations")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Violation.class)))
  public Uni<Paged<Violation>> get(@BeanParam PageRequest pageRequest) {
    return violationService.findAll(pageRequest.size, pageRequest.page);
  }
}

package io.github.jelilio.smbackend.usermanager.web.rest.admin;

import io.github.jelilio.smbackend.common.dto.CourseReq;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.entity.Course;
import io.github.jelilio.smbackend.usermanager.service.CourseService;
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
@Path("/api/admin/courses")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class CoursesResource {
  private static final Logger logger = LoggerFactory.getLogger(CoursesResource.class);

  @Inject
  CourseService courseService;

  @GET
  @Path("/all")
  @Operation(summary = "Fetch a list of courses")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Course.class)))
  public Uni<List<Course>> getAll() {
    return courseService.findAll();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch an course by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Course.class)))
  public Uni<Response> getById(@PathParam("id") String id) {
    return courseService.findById(id).onItem().transform(item -> Response.ok().entity(item).build());
  }

  @GET
  @Path("")
  @Operation(summary = "Fetch a paginated list of courses")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Course.class)))
  public Uni<Paged<Course>> getAll(@BeanParam PageRequest pageRequest) {
    return courseService.findAll(pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  @Operation(summary = "Create a course")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Course.class)))
  public Uni<Response> create(@Valid @RequestBody CourseReq dto) {
    logger.debug("create course: {}", dto);

    return courseService.create(dto)
        .map(inserted -> Response
            .created(URI.create("/api/courses/" + inserted.id))
            .entity(inserted)
            .build());
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Update a course")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Course.class)))
  public Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody CourseReq dto) {
    logger.debug("create course: {}", dto);

    return courseService.update(id, dto)
        .map(inserted -> Response
            .ok()
            .entity(inserted)
            .build());
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete an course by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("id") String id) {
    logger.info("about to delete: {}", id);
    return courseService.delete(id).onItem().transform(item -> Response.ok().build());
  }
}

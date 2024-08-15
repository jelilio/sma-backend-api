package io.github.jelilio.smbackend.apigateway.web.rest.usermanager.admin;

import io.github.jelilio.smbackend.apigateway.client.usermanager.admin.UmSchoolsProxy;
import io.github.jelilio.smbackend.common.dto.SchoolReq;
import io.github.jelilio.smbackend.common.dto.response.SchoolRes;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Uni;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/usermanager/api/admin/schools")
public class UmSchoolsApi {
  @Inject
  @RestClient
  UmSchoolsProxy schoolsProxy;

  @GET
  @Path("/all")
  @Operation(summary = "Fetch a list of schools")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = SchoolRes.class)))
  public Uni<List<SchoolRes>> getAll() {
    return schoolsProxy.findAll();
  }
  
  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch an school by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = SchoolRes.class)))
  public Uni<Response> getById(@PathParam("id") String id) {
    return schoolsProxy.getById(id);
  }

  @GET
  @Path("")
  @Operation(summary = "Fetch a paginated list of schools")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = SchoolRes.class)))
  public Uni<Paged<SchoolRes>> getAll(@BeanParam PageRequest pageRequest) {
    return schoolsProxy.findAll(pageRequest);
  }

  @POST
  @Path("")
  @Operation(summary = "Create an school")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = SchoolRes.class)))
  public Uni<Response> create(@Valid @RequestBody SchoolReq dto) {
    return schoolsProxy.create(dto);
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Create an school")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = SchoolRes.class)))
  public Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody SchoolReq dto) {
    return schoolsProxy.update(id, dto);
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete an school by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("id") String id) {
    return schoolsProxy.delete(id);
  }
}

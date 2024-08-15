package io.github.jelilio.smbackend.apigateway.web.rest.usermanager.admin;

import io.github.jelilio.smbackend.apigateway.client.usermanager.admin.UmInstitutionsProxy;
import io.github.jelilio.smbackend.common.dto.InstitutionReq;
import io.github.jelilio.smbackend.common.dto.response.InstitutionRes;
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
@Path("/usermanager/api/admin/institutions")
public class UmInstitutionsApi {
  @Inject
  @RestClient
  UmInstitutionsProxy institutionsProxy;

  @GET
  @Path("/all")
  @Operation(summary = "Fetch a list of institutions")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = InstitutionRes.class)))
  public Uni<List<InstitutionRes>> getAll() {
    return institutionsProxy.findAll();
  }

  @GET
  @Path("/{id}")
  @Operation(summary = "Fetch an institution by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = InstitutionRes.class)))
  public Uni<Response> getById(@PathParam("id") String id) {
    return institutionsProxy.getById(id);
  }

  @GET
  @Path("")
  @Operation(summary = "Fetch a paginated list of institutions")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = InstitutionRes.class)))
  public Uni<Paged<InstitutionRes>> getAll(@BeanParam PageRequest pageRequest) {
    return institutionsProxy.findAll(pageRequest);
  }

  @POST
  @Path("")
  @Operation(summary = "Create an institution")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = InstitutionRes.class)))
  public Uni<Response> create(@Valid @RequestBody InstitutionReq dto) {
    return institutionsProxy.create(dto);
  }

  @PUT
  @Path("/{id}")
  @Operation(summary = "Create an institution")
  @APIResponse(responseCode = "201", description = "User registration",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = InstitutionRes.class)))
  public Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody InstitutionReq dto) {
    return institutionsProxy.update(id, dto);
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete an institution by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("id") String id) {
    return institutionsProxy.delete(id);
  }
}

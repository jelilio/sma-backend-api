package io.github.jelilio.smbackend.apigateway.web.rest.botmanager;

import io.github.jelilio.smbackend.apigateway.client.botmanager.BmCategorizerProxy;
import io.github.jelilio.smbackend.common.dto.CategorizerDto;
import io.github.jelilio.smbackend.common.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.common.dto.response.CategorizerItemRes;
import io.github.jelilio.smbackend.common.dto.response.CategorizerRes;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
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
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/botmanager/api/categorizers")
public class BmCategorizerApi {
  private static final Logger logger = LoggerFactory.getLogger(BmCategorizerApi.class);

  @Inject
  @RestClient
  BmCategorizerProxy bmCategorizerProxy;

  @GET
  @Path("/{id}")
  public Uni<CategorizerRes> findOne(@PathParam("id") String id) {
    logger.debug("findOne");
    return bmCategorizerProxy.findOne(id);
  }

  @GET
  @Path("/all")
  public Uni<List<CategorizerRes>> get(@QueryParam("lang") Language language) {
    logger.debug("get");
    return bmCategorizerProxy.fetchAll(language);
  }

  @GET
  @Path("")
  public Uni<Paged<CategorizerRes>> get(@BeanParam PageRequest pageRequest) {
    logger.debug("get");
    return bmCategorizerProxy.fetchAll(pageRequest);
  }

  @POST
  @Path("")
  public Uni<Response> create(@Valid @RequestBody CategorizerDto dto) {
    return bmCategorizerProxy.create(dto);
  }

  @PUT
  @Path("/{id}")
  public Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody CategorizerDto dto) {
    return bmCategorizerProxy.update(id, dto);
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete a categorizer by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("id") String id) {
    return bmCategorizerProxy.delete(id);
  }

  @GET
  @Path("/{categorizerId}/items/all")
  public Uni<List<CategorizerItemRes>> getCategorizerItems(@PathParam("categorizerId") String id) {
    logger.debug("findAllItems");
    return bmCategorizerProxy.getCategorizerItems(id);
  }

  @GET
  @Path("/{categorizerId}/items")
  public Uni<Paged<CategorizerItemRes>> getCategorizerItems(@PathParam("categorizerId") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("findAllItems");
    return bmCategorizerProxy.getCategorizerItems(id, pageRequest);
  }

  @POST
  @Path("/{categorizerId}/items")
  public Uni<Response> create(@PathParam("categorizerId") String categorizerId, @Valid @RequestBody CategorizerItemDto dto) {
    return bmCategorizerProxy.create(categorizerId, dto);
  }

  @PUT
  @Path("/{categorizerId}/items/{id}")
  public Uni<Response> update(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id, @Valid @RequestBody CategorizerItemDto dto) {
    return bmCategorizerProxy.update(categorizerId, id, dto);
  }

  @DELETE
  @Path("/{categorizerId}/items/{id}")
  @Operation(summary = "Delete a categorizer-item by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id) {
    return bmCategorizerProxy.delete(categorizerId, id);
  }
}

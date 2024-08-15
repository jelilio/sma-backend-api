package io.github.jelilio.smbackend.botmanager.web.rest;

import io.github.jelilio.smbackend.botmanager.dto.CategorizerDto;
import io.github.jelilio.smbackend.botmanager.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.botmanager.utils.PageRequest;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Path("/api/categorizers")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class CategorizerResource {
  private static final Logger logger = LoggerFactory.getLogger(CategorizerResource.class);

  @Inject
  CategorizerService categorizerService;
  @Inject
  CategorizerItemService categorizerItemService;

  @GET
  @Path("/{id}")
  public Categorizer findOne(@PathParam("id") String id) {
    logger.debug("findOne");
    return categorizerService.findById(id);
  }

  @GET
  @Path("/all")
  public List<Categorizer> get(@QueryParam("lang") Language language) {
    logger.debug("get all");
    return categorizerService.findAll(language);
  }

  @GET
  @Path("")
  public Paged<Categorizer> get(@BeanParam PageRequest pageRequest) {
    logger.debug("get");
    return categorizerService.findAll(pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  public Response create(@Valid @RequestBody CategorizerDto dto) {
    var it = categorizerService.create(dto);

    return Response.created(URI.create(String.format("/api/categorizers/%s", it.id)))
        .build();
  }

  @PUT
  @Path("/{id}")
  public Response update(@PathParam("id") String id, @Valid @RequestBody CategorizerDto dto) {
    var it = categorizerService.update(id, dto);

    return Response.ok().entity(it).build();
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete a categorizer by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Response delete(@PathParam("id") String id) {
    logger.info("about to delete: {}", id);
    categorizerService.delete(id);

    return Response.ok().build();
  }

  @GET
  @Path("/{id}/items/all")
  public List<CategorizerItem> getCategorizerItems(@PathParam("id") String id) {
    logger.debug("findAllItems");
    var categorizer = categorizerService.findById(id);
    return categorizerItemService.findAllItems(categorizer);
  }

  @GET
  @Path("/{id}/items")
  public Paged<CategorizerItem> getCategorizerItems(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("findAllItems");
    var categorizer = categorizerService.findById(id);
    return categorizerItemService.findAllItems(categorizer, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("/{categorizerId}/items")
  public Response create(@PathParam("categorizerId") String categorizerId, @Valid @RequestBody CategorizerItemDto dto) {

    var categorizer = categorizerService.findById(categorizerId);
    var updated = categorizerItemService.create(categorizer, dto);

    return Response.created(URI.create(String.format("/api/categorizers/%s/items", updated.id)))
        .build();
  }

  @PUT
  @Path("/{categorizerId}/items/{id}")
  public Response update(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id, @Valid @RequestBody CategorizerItemDto dto) {
    var categorizer = categorizerService.findById(categorizerId);
    var item = categorizerItemService.update(categorizer, id, dto);

    return Response.ok().entity(item).build();
  }

  @DELETE
  @Path("/{categorizerId}/items/{id}")
  @Operation(summary = "Delete a categorizer item by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Response delete(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id) {
    logger.info("about to delete: {}", id);
    var categorizer = categorizerService.findById(categorizerId);
    categorizerItemService.delete(categorizer, id);
    return Response.ok().build();
  }
}

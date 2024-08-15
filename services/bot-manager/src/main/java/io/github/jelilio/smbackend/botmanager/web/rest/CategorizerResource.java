package io.github.jelilio.smbackend.botmanager.web.rest;

import io.github.jelilio.smbackend.botmanager.entity.Categorizer;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.common.dto.CategorizerDto;
import io.github.jelilio.smbackend.common.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
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
  public Uni<Categorizer> findOne(@PathParam("id") String id) {
    logger.debug("findOne");
    return categorizerService.findById(id);
  }

  @GET
  @Path("/all")
  public Uni<List<Categorizer>> get(@QueryParam("lang") Language language) {
    logger.debug("get all");
    return categorizerService.findAll(language);
  }

  @GET
  @Path("")
  public Uni<Paged<Categorizer>> get(@BeanParam PageRequest pageRequest) {
    logger.debug("get");
    return categorizerService.findAll(pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  public Uni<Response> create(@Valid @RequestBody CategorizerDto dto) {
    return categorizerService.create(dto).onItem()
        .transform(it -> Response.created(URI.create(String.format("/api/categorizers/%s", it.id)))
            .build());
  }

  @PUT
  @Path("/{id}")
  public Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody CategorizerDto dto) {
    return categorizerService.update(id, dto).onItem()
        .transform(it -> Response.ok().build());
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete a categorizer by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("id") String id) {
    logger.info("about to delete: {}", id);
    return categorizerService.delete(id).onItem().transform(item -> Response.ok().build());
  }

  @GET
  @Path("/{id}/items/all")
  public Uni<List<CategorizerItem>> getCategorizerItems(@PathParam("id") String id) {
    logger.debug("findAllItems");
    return categorizerService.findById(id)
        .flatMap(categorizer -> categorizerItemService.findAllItems(categorizer));
  }

  @GET
  @Path("/{id}/items")
  public Uni<Paged<CategorizerItem>> getCategorizerItems(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("findAllItems");
    return categorizerService.findById(id)
        .flatMap(categorizer -> categorizerItemService.findAllItems(categorizer, pageRequest.size, pageRequest.page));
  }

  @POST
  @Path("/{categorizerId}/items")
  public Uni<Response> create(@PathParam("categorizerId") String categorizerId, @Valid @RequestBody CategorizerItemDto dto) {
    return categorizerService.findById(categorizerId)
        .flatMap(categorizer -> categorizerItemService.create(categorizer, dto)
            .onItem().transform(it -> Response.created(URI.create(String.format("/api/categorizers/%s/items", it.id)))
            .build()));
  }

  @PUT
  @Path("/{categorizerId}/items/{id}")
  public Uni<Response> update(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id, @Valid @RequestBody CategorizerItemDto dto) {
    return categorizerService.findById(categorizerId)
        .flatMap(categorizer -> categorizerItemService.update(categorizer, id, dto)
            .onItem().transform(it -> Response.ok().build()));
  }

  @DELETE
  @Path("/{categorizerId}/items/{id}")
  @Operation(summary = "Delete a categorizer item by id")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON))
  public Uni<Response> delete(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id) {
    logger.info("about to delete: {}", id);
    return categorizerService.findById(categorizerId)
        .flatMap(categorizer -> categorizerItemService.delete(categorizer, id)
            .onItem().transform(item -> Response.ok().build()));
  }
}

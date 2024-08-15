package io.github.jelilio.smbackend.botmanager.web.rest;

import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.service.BotActionService;
import io.github.jelilio.smbackend.botmanager.service.BotService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.common.dto.BotActionDto;
import io.github.jelilio.smbackend.common.dto.BotDto;
import io.github.jelilio.smbackend.common.dto.response.PostRes;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@Path("/api/bots")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BotResource {
  private static final Logger logger = LoggerFactory.getLogger(BotResource.class);

  @Inject
  BotService botService;
  @Inject
  BotActionService botActionService;
  @Inject
  CategorizerService categorizerService;
  @Inject
  CategorizerItemService categorizerItemService;

  @GET
  @Path("/{id}")
  public Uni<Bot> findOne(@PathParam("id") String id) {
    logger.debug("findOne: id: {}", id);
    return botService.findById(id);
  }

  @GET
  @Path("")
  public Uni<Paged<Bot>> get(@BeanParam PageRequest pageRequest) {
    return botService.findAll(pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  public Uni<Response> create(@RequestBody BotDto botDto) {
    logger.debug("create: botDto: {}", botDto);
    return botService.createBot(botDto).onItem()
        .transform(it -> Response.created(URI.create(String.format("/api/bots/%s", it.id)))
            .build());
  }

  @PUT
  @Path("/{id}")
  public Uni<Response> update(@PathParam("id") String id, @RequestBody BotDto botDto) {
    logger.debug("update: id: {}, botDto: {}", id, botDto);
    return botService.updateBot(id, botDto).onItem()
        .transform(it -> Response.ok().build());
  }

  @GET
  @Path("/{botId}/items/all")
  public Uni<List<CategorizerItem>> getCategorizerItems(@PathParam("botId") String id) {
    logger.debug("findAllItems byt bot");
    return botService.findById(id)
        .flatMap(bot -> categorizerService.findById(bot.categorizer.id.toString())
            .flatMap(categorizer -> categorizerItemService.findAllItems(categorizer)));
  }

  @GET
  @Path("/{id}/actions")
  public Uni<Paged<BotAction>> getCategorizerItems(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("findAllItems");
    return botService.findById(id)
        .flatMap(bot -> botActionService.findAllActions(bot, pageRequest.size, pageRequest.page));
  }

  @DELETE
  @Path("/{id}/actions/{action}/items/{itemId}")
  public Uni<Void> deleteBotAction(@PathParam("id") String id, @PathParam("itemId") String itemId, @PathParam("action")Action action) {
    logger.debug("deleteBotAction: id: {}, topActors: {}, itemId: {}", id, itemId, action);
    return botService.findById(id)
        .flatMap(bot -> botActionService.delete(bot, action, itemId));
  }

  @POST
  @Path("/{botId}/actions")
  public Uni<Response> create(@PathParam("botId") String categorizerId, @Valid @RequestBody BotActionDto dto) {
    return botService.findById(categorizerId)
        .flatMap(bot -> botActionService.create(bot, dto)
            .onItem().transform(it -> Response.created(URI.create(String.format("/api/bots/%s/actions", it.id)))
                .build()));
  }

  @POST
  @Path("/analyzepost")
  public Uni<Response> analyzePost(@RequestBody PostRes botDto) {
    logger.debug("analyzePost: botDto: {}", botDto);
    return botService.analyzePost(botDto).onItem()
        .transform(it -> Response.ok().entity(it)
            .build());
  }
}

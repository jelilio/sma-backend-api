package io.github.jelilio.smbackend.botmanager.web.rest;

import io.github.jelilio.smbackend.botmanager.dto.BotActionDto;
import io.github.jelilio.smbackend.botmanager.dto.BotDto;
import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.service.BotActionService;
import io.github.jelilio.smbackend.botmanager.service.BotService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.botmanager.utils.PageRequest;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.PostObject;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
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
  public Bot findOne(@PathParam("id") String id) {
    logger.debug("findOne: id: {}", id);
    return botService.findById(id);
  }

  @GET
  @Path("")
  public Paged<Bot> get(@BeanParam PageRequest pageRequest) {
    return botService.findAll(pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("")
  public Response create(@RequestBody BotDto botDto) {
    logger.debug("create: botDto: {}", botDto);
    var it = botService.createBot(botDto);
    return Response.created(URI.create(String.format("/api/bots/%s", it.id)))
        .build();
  }

  @PUT
  @Path("/{id}")
  public Response update(@PathParam("id") String id, @RequestBody BotDto botDto) {
    logger.debug("update: id: {}, botDto: {}", id, botDto);
    var it = botService.updateBot(id, botDto);
    return Response.ok().entity(it).build();
  }

  @GET
  @Path("/{botId}/items/all")
  public List<CategorizerItem> getCategorizerItems(@PathParam("botId") String id) {
    logger.debug("findAllItems byt bot");
    var bot = botService.findById(id);
    var categorizer = categorizerService.findById(bot.categorizer.id.toString());
    return categorizerItemService.findAllItems(categorizer);
  }

  @GET
  @Path("/{id}/actions")
  public Paged<BotAction> getCategorizerItems(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("findAllItems");
    var bot = botService.findById(id);
    return botActionService.findAllActions(bot, pageRequest.size, pageRequest.page);
  }

  @DELETE
  @Path("/{id}/actions/{action}/items/{itemId}")
  public Response deleteBotAction(@PathParam("id") String id, @PathParam("itemId") String itemId, @PathParam("action")Action action) {
    logger.debug("deleteBotAction: id: {}, topActors: {}, itemId: {}", id, itemId, action);
    var bot = botService.findById(id);
    botActionService.delete(bot, action, itemId);
    return Response.ok().build();
  }

  @POST
  @Path("/{botId}/actions")
  public Response create(@PathParam("botId") String botId, @Valid @RequestBody BotActionDto dto) {
    logger.info("create: botId: {}, dto: {}", botId, dto);
    var bot = botService.findById(botId);
    var it = botActionService.create(bot, dto);
    return Response.created(URI.create(String.format("/api/bots/%s/actions", it.id)))
        .build();
  }

  @POST
  @Path("/analyze-post")
  public Response analyzePost(@RequestBody PostObject botDto) {
    logger.debug("analyzePost: botDto: {}", botDto);
    var it = botService.analyzePost(botDto);
    return Response.ok().entity(it)
        .build();
  }
}

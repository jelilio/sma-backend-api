package io.github.jelilio.smbackend.apigateway.web.rest.botmanager;

import io.github.jelilio.smbackend.apigateway.client.botmanager.BmBotProxy;
import io.github.jelilio.smbackend.common.dto.BotActionDto;
import io.github.jelilio.smbackend.common.dto.BotDto;
import io.github.jelilio.smbackend.common.dto.response.BotActionRes;
import io.github.jelilio.smbackend.common.dto.response.BotRes;
import io.github.jelilio.smbackend.common.dto.response.CategorizerItemRes;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/botmanager/api/bots")
public class BmBotsApi {
  private static final Logger logger = LoggerFactory.getLogger(BmBotsApi.class);

  @Inject
  @RestClient
  BmBotProxy bmBotProxy;

  @GET
  @Path("/{id}")
  public Uni<BotRes> findOne(@PathParam("id") String id) {
    logger.debug("findOne: id: {}", id);
    return bmBotProxy.findOne(id);
  }

//  @GET
//  @Path("/all")
//  public Uni<List<BotRes>> getAll(@QueryParam("type") ModelType type, @QueryParam("lang") Language language) {
//    logger.info("get: type: {}, {}", type, language);
//    return bmBotProxy.fetchAll(type, language);
//  }
@PUT
@Path("/{id}")
public Uni<Response> update(@PathParam("id") String id, @RequestBody BotDto botDto) {
  logger.debug("update: id: {}, botDto: {}", id, botDto);
  return bmBotProxy.update(id, botDto);
}

  @POST
  @Path("")
  public Uni<Response> create(@Valid @RequestBody BotDto dto) {
    return bmBotProxy.create(dto);
  }

  @GET
  @Path("/{botId}/items/all")
  public Uni<List<CategorizerItemRes>> getCategorizerItems(@PathParam("botId") String id) {
    logger.debug("findAllItems byt bot");
    return bmBotProxy.getCategorizerItems(id);
  }

  @GET
  @Path("")
  public Uni<Paged<BotRes>> get(@BeanParam PageRequest pageRequest) {
    logger.debug("get: paginated bots");
    return bmBotProxy.fetchAll(pageRequest);
  }

  @GET
  @Path("/{id}/actions")
  public Uni<Paged<BotActionRes>> getCategorizerItems(@PathParam("id") String id, @BeanParam PageRequest pageRequest) {
    logger.debug("findAllItems");
    return bmBotProxy.fetchBotActions(id, pageRequest);
  }

  @DELETE
  @Path("/{id}/actions/{action}/items/{itemId}")
  public Uni<Void> deleteBotAction(@PathParam("id") String id, @PathParam("itemId") String itemId, @PathParam("action") Action action) {
    logger.info("deleteBotAction: {}, {}, {}", id, itemId, action);
    return bmBotProxy.deleteBotAction(id, itemId, action);
  }

  @POST
  @Path("/{botId}/actions")
  public Uni<Response> create(@PathParam("botId") String botId, @Valid @RequestBody BotActionDto dto) {
    return bmBotProxy.create(botId, dto);
  }

}

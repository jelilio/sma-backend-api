package io.github.jelilio.smbackend.apigateway.client.botmanager;

import io.github.jelilio.smbackend.common.dto.BotActionDto;
import io.github.jelilio.smbackend.common.dto.BotDto;
import io.github.jelilio.smbackend.common.dto.response.BotActionRes;
import io.github.jelilio.smbackend.common.dto.response.BotRes;
import io.github.jelilio.smbackend.common.dto.response.CategorizerItemRes;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/bots")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "botmanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface BmBotProxy {
  @GET
  @Path("/{id}")
  Uni<BotRes> findOne(@PathParam("id") String id);

  @PUT
  @Path("/{id}")
  Uni<Response> update(@PathParam("id") String id, @RequestBody BotDto botDto);

  @POST
  @Path("")
  Uni<Response> create(@Valid @RequestBody BotDto dto);

  @GET
  @Path("/{botId}/items/all")
  Uni<List<CategorizerItemRes>> getCategorizerItems(@PathParam("botId") String id);

  @GET
  @Path("")
  Uni<Paged<BotRes>> fetchAll(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/{id}/actions")
  Uni<Paged<BotActionRes>> fetchBotActions(@PathParam("id") String id, @BeanParam PageRequest pageRequest);

  @DELETE
  @Path("/{id}/actions/{action}/items/{itemId}")
  Uni<Void> deleteBotAction(@PathParam("id") String id, @PathParam("itemId") String itemId, @PathParam("action") Action action);

  @POST
  @Path("/{botId}/actions")
  Uni<Response> create(@PathParam("botId") String botId, @Valid @RequestBody BotActionDto dto);
}

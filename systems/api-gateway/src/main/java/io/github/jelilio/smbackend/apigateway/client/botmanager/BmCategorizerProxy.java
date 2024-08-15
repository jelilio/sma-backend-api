package io.github.jelilio.smbackend.apigateway.client.botmanager;

import io.github.jelilio.smbackend.common.dto.CategorizerDto;
import io.github.jelilio.smbackend.common.dto.CategorizerItemDto;
import io.github.jelilio.smbackend.common.dto.response.CategorizerItemRes;
import io.github.jelilio.smbackend.common.dto.response.CategorizerRes;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
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

@Path("/api/categorizers")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "botmanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface BmCategorizerProxy {
  @GET
  @Path("/{id}")
  Uni<CategorizerRes> findOne(@PathParam("id") String id);

  @GET
  @Path("/all")
  Uni<List<CategorizerRes>> fetchAll(@QueryParam("lang") Language language);

  @GET
  @Path("")
  Uni<Paged<CategorizerRes>> fetchAll(@BeanParam PageRequest pageRequest);

  @POST
  @Path("")
  Uni<Response> create(@Valid @RequestBody CategorizerDto dto);

  @PUT
  @Path("/{id}")
  Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody CategorizerDto dto);

  @DELETE
  @Path("{id}")
  Uni<Response> delete(@PathParam("id") String id);

  @GET
  @Path("/{id}/items/all")
  Uni<List<CategorizerItemRes>> getCategorizerItems(@PathParam("id") String id);

  @GET
  @Path("/{categorizerId}/items")
  Uni<Paged<CategorizerItemRes>> getCategorizerItems(@PathParam("categorizerId") String id, @BeanParam PageRequest pageRequest);

  @POST
  @Path("/{categorizerId}/items")
  Uni<Response> create(@PathParam("categorizerId") String id, @Valid @RequestBody CategorizerItemDto dto);

  @PUT
  @Path("/{categorizerId}/items/{id}")
  Uni<Response> update(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id, @Valid @RequestBody CategorizerItemDto dto);

  @DELETE
  @Path("/{categorizerId}/items/{id}")
  Uni<Response> delete(@PathParam("categorizerId") String categorizerId, @PathParam("id") String id);
}

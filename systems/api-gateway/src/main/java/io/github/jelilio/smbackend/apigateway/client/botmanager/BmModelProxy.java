package io.github.jelilio.smbackend.apigateway.client.botmanager;

import io.github.jelilio.smbackend.common.dto.response.ModelRes;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "botmanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface BmModelProxy {
  @GET
  @Path("/models/{id}")
  Uni<ModelRes> findOne(@PathParam("id") String id, @QueryParam("type") ModelType type);

  @GET
  @Path("/models/all")
  Uni<List<ModelRes>> fetchAll(@QueryParam("type") ModelType type, @QueryParam("lang") Language language);


  @GET
  @Path("/models")
  Uni<Paged<ModelRes>> fetchAll(@QueryParam("type") ModelType type, @BeanParam PageRequest pageRequest);
}

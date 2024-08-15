package io.github.jelilio.smbackend.apigateway.client.newsfeed.user;

import io.github.jelilio.smbackend.common.dto.PostDto;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.PostProRes;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/api/user/posts/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfPostProxy {
  @POST
  @Path("/{id}/share")
  Uni<Response> share(@PathParam("id") String id);

  @POST
  @Path("/{id}/unshared")
  Uni<Response> unshared(@PathParam("id") String id);

  @POST
  @Path("/{id}/like")
  Uni<Response> like(@PathParam("id") String id);

  @POST
  @Path("/{id}/unliked")
  Uni<Response> unliked(@PathParam("id") String id);

  @GET
  @Path("/my")
  Uni<Paged<PostProRes>> get(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/all")
  Uni<Paged<PostProRes>> all(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/search")
  Uni<Paged<PostProRes>> search(@QueryParam("q") String text, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/followed")
  Uni<Paged<PostProRes>> followed(@BeanParam PageRequest pageRequest);

  @GET
  @Path("/communities")
  Uni<Paged<PostProRes>> followedCommunities(@BeanParam PageRequest pageRequest);

  @POST
  @Path("")
  @Consumes(MULTIPART_FORM_DATA)
  Uni<Response> create(@Valid @BeanParam PostDto dto);

  @POST
  @Path("/{id}/replies")
  @Consumes(MULTIPART_FORM_DATA)
  Uni<Response> reply(@PathParam("id") String id, @Valid @BeanParam PostDto dto);

  @GET
  @Path("/{id}/replies")
  Uni<Paged<PostProRes>> replies(@PathParam("id") String id, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/{id}")
  Uni<Response> get(@PathParam("id") String id);

  @DELETE
  @Path("/{id}")
  Uni<Response> delete(@PathParam("id") String id);
}

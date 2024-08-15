package io.github.jelilio.smbackend.apigateway.client.usermanager.admin;

import io.github.jelilio.smbackend.common.dto.FileDto;
import io.github.jelilio.smbackend.common.dto.UserReq;
import io.github.jelilio.smbackend.common.dto.response.EnableUserReq;
import io.github.jelilio.smbackend.common.dto.response.UserRes;
import io.github.jelilio.smbackend.common.entity.enumeration.UserQueryType;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/api/admin/users/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "usermanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface UmUsersProxy {

  @GET
  @Path("")
  Uni<Paged<UserRes>> findUsers(@RestQuery("type") UserType type, @BeanParam PageRequest pageRequest);

  @GET
  @Path("/all")
  Uni<List<UserRes>> findAllUsers(@RestQuery("type") UserType type, @RestQuery("queryType") UserQueryType queryType);

  @GET
  @Path("{id}")
  Uni<UserRes> getById(@PathParam("id") String id);

  @POST
  @Path("")
  Uni<Response> create(@NotNull @RestQuery("type") UserType type, @Valid @RequestBody UserReq dto);

  @PUT
  @Path("/{id}")
  Uni<Response> update(@PathParam("id") String id, @NotNull @QueryParam("type") UserType type, @Valid @RequestBody UserReq dto);

  @PUT
  @Path("/{id}/enable")
  Uni<UserRes> enableOrDisable(@PathParam("id") String id, @Valid @RequestBody EnableUserReq enableUser);

  @POST
  @Path("/batch-load")
  @Produces(APPLICATION_JSON)
  @Consumes(MULTIPART_FORM_DATA)
  Uni<Response> batchLoad(@Valid @BeanParam FileDto dto);

  @POST
  @Path("/{id}/approve-verification")
  Uni<String> approveVerification(@PathParam("id") String id);
}

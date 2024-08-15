package io.github.jelilio.smbackend.apigateway.client.usermanager;

import io.github.jelilio.smbackend.common.dto.PhotoDto;
import io.github.jelilio.smbackend.common.dto.UserBioUpdateDto;
import io.github.jelilio.smbackend.common.dto.UsernameUpdateDto;
import io.github.jelilio.smbackend.common.dto.response.PhotoRes;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("/api/user")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "usermanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface UmUserProxy {
  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<Response> info();

  @PUT
  @Path("/username")
  Uni<Response> updateUsername(@Valid @RequestBody UsernameUpdateDto dto);

  @PUT
  @Path("/name")
  Uni<Response> updateName(@Valid @RequestBody UserBioUpdateDto dto);

  @GET
  @Path("/avatar")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Uni<Response> stream();

  @PUT
  @Path("/avatar")
  @Consumes(MULTIPART_FORM_DATA)
  Uni<PhotoRes> uploadAvatar(@Valid @BeanParam PhotoDto dto);

  @PUT
  @Path("/request-verification")
  Uni<Response> requestVerification();
}

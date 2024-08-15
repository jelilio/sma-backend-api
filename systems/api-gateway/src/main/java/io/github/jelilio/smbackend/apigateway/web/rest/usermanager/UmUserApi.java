package io.github.jelilio.smbackend.apigateway.web.rest.usermanager;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.user.NfUserProxy;
import io.github.jelilio.smbackend.apigateway.client.usermanager.UmUserProxy;
import io.github.jelilio.smbackend.common.dto.PhotoDto;
import io.github.jelilio.smbackend.common.dto.UsernameUpdateDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/usermanager/api/user")
public class UmUserApi {
  @Inject
  @RestClient
  UmUserProxy userProxy;

  @Inject
  @RestClient
  NfUserProxy nfUserProxy;

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> info() {
    return userProxy.info();
  }

  @PUT
  @Path("/username")
  public Uni<Response> updateUsername(@Valid @RequestBody UsernameUpdateDto dto) {
    return userProxy.updateUsername(dto);
  }

  @GET
  @Path("/avatar")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Uni<Response> streamAvatar() {
    return userProxy.stream();
  }

  @PUT
  @Path("/avatar")
  @Consumes(MULTIPART_FORM_DATA)
  public Uni<Response> uploadAvatar(@Valid @BeanParam PhotoDto dto) {
    return userProxy.uploadAvatar(dto).flatMap(res -> {
      return nfUserProxy.updateAvatar(res);
    });
  }
}
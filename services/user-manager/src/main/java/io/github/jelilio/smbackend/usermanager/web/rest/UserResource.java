package io.github.jelilio.smbackend.usermanager.web.rest;


import io.github.jelilio.smbackend.common.dto.PhotoDto;
import io.github.jelilio.smbackend.common.dto.UserBioUpdateDto;
import io.github.jelilio.smbackend.common.dto.UserUpdateDto;
import io.github.jelilio.smbackend.common.dto.UsernameUpdateDto;
import io.github.jelilio.smbackend.common.dto.response.PhotoRes;
import io.github.jelilio.smbackend.usermanager.client.image.ImageProxy;
import io.github.jelilio.smbackend.usermanager.entity.User;
import io.github.jelilio.smbackend.usermanager.model.KUser;
import io.github.jelilio.smbackend.usermanager.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static io.github.jelilio.smbackend.common.utils.Constants.USER_ID;
import static io.github.jelilio.smbackend.common.utils.Constants.USER_USERNAME;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@WithSession
@RequestScoped
@Path("/api/user")
@RolesAllowed("newsfeed.ROLE_USER")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
  private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

  @Inject
  UserService userService;

  @Claim(USER_ID)
  String subject;

  @Claim(USER_USERNAME)
  String username;

  @Inject
  SecurityIdentity securityIdentity;

  @GET
  @Path("")
  @RolesAllowed("ROLE_USER")
  public Uni<KUser> info() {
    return userService.findOne(subject);
  }

  @PUT
  @Path("")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Uni<Response> update(@Valid @BeanParam UserUpdateDto dto) {
    Uni<User> userUni = userService.findByOidcId(subject);

    return userUni
        .flatMap(user -> userService.update(user, dto))
        .map(inserted -> Response.ok().build());
  }

  @PUT
  @Path("/name")
  public Uni<Response> updateName(@Valid @RequestBody UserBioUpdateDto dto) {
    Uni<User> userUni = userService.findByOidcId(subject);

    return userUni
        .flatMap(user -> userService.updateName(user, dto)
            .onItem().transform(response -> Response.ok(response).build()));
  }

  @PUT
  @Path("/username")
  public Uni<Response> updateUsername(@Valid @RequestBody UsernameUpdateDto dto) {
    Uni<User> userUni = userService.findByOidcId(subject);

    return userUni
        .flatMap(user -> userService.updateUsername(user, dto.username())
            .onItem().transform(response -> Response.ok(response).build()));
  }

  @PUT
  @Path("/request-verification")
  public Uni<Response> requestVerification() {
    Uni<User> userUni = userService.findByOidcId(subject);

    return userUni
        .flatMap(user -> userService.requestVerification(user)
            .onItem().transform(response -> Response.ok(response).build()));
  }

  @GET
  @Path("/avatar")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Uni<Response> getFile() {
    return userService.findByOidcId(subject)
        .flatMap(user -> {
          var url = user.avatarUrl == null? "https://i.imgur.com/5mxPPAC.jpg" :
              user.avatarUrl;

          ImageProxy remoteApi = RestClientBuilder.newBuilder()
              .baseUri(URI.create(url))
              .build(ImageProxy.class);

          return remoteApi.stream();
        });
  }

  @PUT
  @Path("/avatar")
  @Consumes(MULTIPART_FORM_DATA)
  public Uni<Response> uploadAvatar(@Valid @BeanParam PhotoDto dto) {
    logger.debug("upload photo: post");

    return userService.updateAvatar(subject, dto)
        .map(updated -> Response
            .ok()
            .entity(new PhotoRes(updated.avatarUrl, updated.avatarType))
            .build());
  }
}

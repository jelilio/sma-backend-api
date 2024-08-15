package io.github.jelilio.smbackend.usermanager.web.rest;


import io.github.jelilio.smbackend.usermanager.client.image.ImageProxy;
import io.github.jelilio.smbackend.usermanager.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@WithSession
@RequestScoped
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
  private static final Logger logger = LoggerFactory.getLogger(UsersResource.class);

  @Inject
  UserService userService;

  @GET
  @Path("/{id}/avatar")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Uni<Response> getFile(@PathParam("id") String id) {
    logger.info("getFile: {}", id);
    return userService.findByOidcId(id)
        .flatMap(user -> {
          var url = user.avatarUrl == null? "https://i.imgur.com/5mxPPAC.jpg" :
              user.avatarUrl;

          ImageProxy remoteApi = RestClientBuilder.newBuilder()
              .baseUri(URI.create(url))
              .build(ImageProxy.class);

          return remoteApi.stream();
        });
  }
}

package io.github.jelilio.smbackend.apigateway.web.rest.usermanager;

import io.github.jelilio.smbackend.apigateway.client.usermanager.UmUsersProxy;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/usermanager/api")
public class UmAccountsApi {
  private static final Logger logger = LoggerFactory.getLogger(UmAccountsApi.class);

  @Inject
  @RestClient
  UmUsersProxy usersProxy;

  @GET
  @Path("/accounts/{id}/avatar")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Uni<Response> avatar(@PathParam("id") String id) {
    logger.info("get avatar: {}", id);
    return usersProxy.stream(id);
  }
}

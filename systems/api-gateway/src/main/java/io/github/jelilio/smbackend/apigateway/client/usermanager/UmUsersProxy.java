package io.github.jelilio.smbackend.apigateway.client.usermanager;

import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api")
@Produces(APPLICATION_JSON)
//@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "usermanager-api")
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface UmUsersProxy {
  @GET
  @Path("/users/{id}/avatar")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Uni<Response> stream(@PathParam("id") String id);
}

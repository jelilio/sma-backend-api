package io.github.jelilio.smbackend.usermanager.client.keycloak;

import io.github.jelilio.smbackend.common.exception.mapper.NotFoundExceptionMapper;
import io.github.jelilio.smbackend.usermanager.model.Client;
import io.github.jelilio.smbackend.usermanager.model.Role;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/clients")
@Produces(APPLICATION_JSON)
//@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "keycloak-api")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(NotFoundExceptionMapper.class)})
public interface ClientProxy {
  @GET
  @Path("")
  Uni<List<Client>> getByClientId(@QueryParam("clientId") String clientId);

  @GET
  @Path("/{clientId}/roles/{name}/composites")
  Uni<List<Role>> getClientRoles(@PathParam("clientId") String clientId, @PathParam("name") String name);
}

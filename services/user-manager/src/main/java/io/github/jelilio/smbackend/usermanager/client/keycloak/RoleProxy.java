package io.github.jelilio.smbackend.usermanager.client.keycloak;

import io.github.jelilio.smbackend.common.exception.mapper.NotFoundExceptionMapper;
import io.github.jelilio.smbackend.usermanager.model.Role;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.Set;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/roles")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "keycloak-api")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(NotFoundExceptionMapper.class)})
public interface RoleProxy {
  @GET
  @Path("/")
  Uni<List<Role>> getRoles();

  @GET
  @Path("/{name}")
  Uni<Role> getRole(@PathParam("name") String name);

  @GET
  @Path("/{name}/composites")
  Uni<List<Role>> getCompositeRoles(@PathParam("name") String name);

  @POST
  @Path("")
  Uni<Response> createRole(@Valid @RequestBody Role role);

  @PUT
  @Path("/{name}")
  Uni<Response> updateRole(@PathParam("name") String name, @Valid @RequestBody Role role);

  @DELETE
  @Path("/{name}")
  Uni<Void> deleteRole(@PathParam("name") String name);

  @POST
  @Path("/{name}/composites")
  Uni<Response> addCompositeRoles(@PathParam("name") String name, @Valid @RequestBody Set<Role> roles);

  @DELETE
  @Path("/{name}/composites")
  Uni<Response> removeCompositeRoles(@PathParam("name") String name, @Valid @RequestBody Set<Role> roles);
}

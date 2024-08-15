package io.github.jelilio.smbackend.usermanager.client.keycloak;

import io.github.jelilio.smbackend.common.exception.mapper.NotFoundExceptionMapper;
import io.github.jelilio.smbackend.usermanager.model.*;
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

@Path("/users")
@Produces(APPLICATION_JSON)
//@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "keycloak-api")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(NotFoundExceptionMapper.class)})
public interface UserProxy {
  @GET
  @Path("")
  Uni<List<KUser>> getUsers();

  @POST
  @Path("")
  Uni<Response> register(@Valid @RequestBody Register register);

  @POST
  @Path("")
  Uni<Response> register(@Valid @RequestBody RegisterWithUsername register);

  @PUT
  @Path("/{userId}")
  Uni<Response> update(@PathParam("userId")String userId, @Valid @RequestBody Register register);

  @PUT
  @Path("/{userId}")
  Uni<Response> updateUsername(@PathParam("userId")String userId, @Valid @RequestBody UpdateUsername body);

  @PUT
  @Path("/{userId}")
  Uni<Response> updateName(@PathParam("userId")String userId, @Valid @RequestBody UpdateName body);

  @PUT
  @Path("/{userId}")
  Uni<Response> updateNameOnly(@PathParam("userId")String userId, @Valid @RequestBody UpdateNameOnly body);

  @PUT
  @Path("/{userId}")
  Uni<Response> disableOrEnable(@PathParam("userId")String userId, @Valid @RequestBody EnableUser body);

  @PUT
  @Path("/{userId}")
  Uni<Response> enableVerifiedEmail(@PathParam("userId")String userId, @Valid @RequestBody EnableVerifyEmail body);

  @GET
  @Path("/{userId}")
  Uni<KUser> getUser(@PathParam("userId") String userId);

  @POST
  @Path("/{userId}/role-mappings/realm")
  Uni<Response> assignRolesToUser(@PathParam("userId") String userId, @Valid @RequestBody Set<Role> roles);

  @GET
  @Path("/{id}/role-mappings/realm")
  Uni<List<Role>> getUserRoles(@PathParam("id") String userId);

  @POST
  @Path("/{userId}/role-mappings/clients/{clientId}")
  Uni<Response> assignClientRolesToUser(@PathParam("userId") String userId, @PathParam("clientId") String clientId, @Valid @RequestBody Set<Role> roles);

}

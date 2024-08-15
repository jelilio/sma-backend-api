package io.github.jelilio.smbackend.apigateway.client.newsfeed.admin;

import io.github.jelilio.smbackend.common.dto.response.EnableUserReq;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.ViolationRes;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/admin/users")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfAdminUsersProxy {
  @PUT
  @Path("/{id}/enable")
  Uni<Response> enableOrDisable(@PathParam("id") String id, @Valid @RequestBody EnableUserReq enableUser);

  @GET
  @Path("/{id}/violations")
  Uni<Paged<ViolationRes>> fetchViolations(@PathParam("id") String userId, @BeanParam PageRequest pageRequest);
}

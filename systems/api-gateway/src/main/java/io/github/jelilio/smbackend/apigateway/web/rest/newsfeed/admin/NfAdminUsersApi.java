package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.admin;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.admin.NfAdminUsersProxy;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.ViolationRes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/admin/users")
public class NfAdminUsersApi {

  @Inject
  @RestClient
  NfAdminUsersProxy nfAdminUsersProxy;

  @GET
  @Path("/{id}/violations")
  public Uni<Paged<ViolationRes>> fetchViolations(@PathParam("id") String userId, @BeanParam PageRequest pageRequest) {
    return nfAdminUsersProxy.fetchViolations(userId, pageRequest);
  }
}

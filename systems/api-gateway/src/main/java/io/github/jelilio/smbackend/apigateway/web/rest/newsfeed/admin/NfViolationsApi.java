package io.github.jelilio.smbackend.apigateway.web.rest.newsfeed.admin;

import io.github.jelilio.smbackend.apigateway.client.newsfeed.admin.NfAdminViolationsProxy;
import io.github.jelilio.smbackend.common.entity.enumeration.UserQueryType;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.ViolationRes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/newsfeed/api/admin/violations")
public class NfViolationsApi {
  @Inject
  @RestClient
  NfAdminViolationsProxy violationsProxy;

  @GET
  @Path("/all")
  public Uni<List<ViolationRes>> findAllUsers(@RestQuery("queryType") UserQueryType queryType) {
    return violationsProxy.fetchAllViolations(queryType);
  }

  @GET
  @Path("/report")
  public Uni<Paged<ViolationRes>> fetchViolations(@RestQuery("queryType") UserQueryType queryType, @BeanParam PageRequest pageRequest) {
    return violationsProxy.getReport(queryType, pageRequest);
  }
}

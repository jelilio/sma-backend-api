package io.github.jelilio.smbackend.apigateway.client.newsfeed.admin;

import io.github.jelilio.smbackend.common.entity.enumeration.UserQueryType;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.ViolationRes;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/admin/violations")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "newsfeed-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface NfAdminViolationsProxy {
  @GET
  @Path("/all")
  Uni<List<ViolationRes>> fetchAllViolations(@RestQuery("queryType") UserQueryType queryType);

  @GET
  @Path("/report")
  Uni<Paged<ViolationRes>> getReport(@RestQuery("queryType") UserQueryType queryType, @BeanParam PageRequest pageRequest);
}

package io.github.jelilio.smbackend.apigateway.client.usermanager.admin;

import io.github.jelilio.smbackend.common.dto.SchoolReq;
import io.github.jelilio.smbackend.common.dto.response.SchoolRes;
import io.github.jelilio.smbackend.common.exception.mapper.ClientExceptionMapper;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/admin/schools/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "usermanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface UmSchoolsProxy {
  @GET
  @Path("all")
  Uni<List<SchoolRes>> findAll();
  
  @GET
  @Path("{id}")
  Uni<Response> getById(@PathParam("id") String id);

  @GET
  @Path("")
  Uni<Paged<SchoolRes>> findAll(@BeanParam PageRequest pageRequest);

  @POST
  @Path("")
  Uni<Response> create(@Valid @RequestBody SchoolReq dto);

  @PUT
  @Path("{id}")
  Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody SchoolReq dto);

  @DELETE
  @Path("{id}")
  Uni<Response> delete(@PathParam("id") String id);
}

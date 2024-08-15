package io.github.jelilio.smbackend.apigateway.client.usermanager.admin;

import io.github.jelilio.smbackend.common.dto.CourseReq;
import io.github.jelilio.smbackend.common.dto.response.CourseRes;
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

@Path("/api/admin/courses/")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RegisterRestClient(configKey = "usermanager-api")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProviders({@RegisterProvider(ClientExceptionMapper.class)})
public interface UmCoursesProxy {
  @GET
  @Path("all")
  Uni<List<CourseRes>> findAll();
  
  @GET
  @Path("{id}")
  Uni<Response> getById(@PathParam("id") String id);

  @GET
  @Path("")
  Uni<Paged<CourseRes>> findAll(@BeanParam PageRequest pageRequest);

  @POST
  @Path("")
  Uni<Response> create(@Valid @RequestBody CourseReq dto);

  @PUT
  @Path("{id}")
  Uni<Response> update(@PathParam("id") String id, @Valid @RequestBody CourseReq dto);

  @DELETE
  @Path("{id}")
  Uni<Response> delete(@PathParam("id") String id);
}

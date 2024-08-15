package io.github.jelilio.smbackend.apigateway.web.rest.botmanager;

import io.github.jelilio.smbackend.apigateway.client.botmanager.BmModelProxy;
import io.github.jelilio.smbackend.common.dto.response.ModelRes;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("/botmanager/api/models")
public class BmModelApi {
  private static final Logger logger = LoggerFactory.getLogger(BmModelApi.class);

  @Inject
  @RestClient
  BmModelProxy bmModelProxy;

  @GET
  @Path("/{id}")
  public Uni<ModelRes> findOne(@PathParam("id") String id, @QueryParam("type") ModelType type) {
    logger.debug("findOne: type: {}", type);
    return bmModelProxy.findOne(id, type);
  }

  @GET
  @Path("/all")
  public Uni<List<ModelRes>> getAll(@QueryParam("type") ModelType type, @QueryParam("lang") Language language) {
    logger.debug("get: type: {}, lang: {}", type, language);
    return bmModelProxy.fetchAll(type, language);
  }

  @GET
  @Path("")
  public Uni<Paged<ModelRes>> get(@QueryParam("type") ModelType type, @BeanParam PageRequest pageRequest) {
    logger.debug("get: type: {}", type);
    return bmModelProxy.fetchAll(type, pageRequest);
  }
}

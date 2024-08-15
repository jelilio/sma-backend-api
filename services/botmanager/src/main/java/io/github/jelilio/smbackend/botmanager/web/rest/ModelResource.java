package io.github.jelilio.smbackend.botmanager.web.rest;

import io.github.jelilio.smbackend.botmanager.entity.Model;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.Language;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.botmanager.service.ModelService;
import io.github.jelilio.smbackend.botmanager.utils.PageRequest;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
//@RolesAllowed("ROLE_ADMIN")
@Path("/api/models")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ModelResource {
  private static final Logger logger = LoggerFactory.getLogger(ModelResource.class);
  @Inject
  ModelService modelService;

//  @Inject
//  CategorizerService categorizerService;

  @GET
  @Path("/all")
  public List<Model> getAll(@QueryParam("type") ModelType type, @QueryParam("lang") Language language) {
    logger.debug("etAll: type: {}, language: {}", type, language);
    return modelService.findAll(type, language);
  }

  @GET
  @Path("/{id}")
  public Model findOne(@PathParam("id") String id, @QueryParam("type") ModelType type) {
    logger.debug("findOne: type: {}", type);
    return modelService.findByIdAndType(id, type);
  }

  @GET
  @Path("")
  public Paged<Model> get(@QueryParam("type") ModelType type, @BeanParam PageRequest pageRequest) {
    logger.debug("get: type: {}", type);
    return modelService.findAll(type, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("/follow")
  public List<String> breakThis(@RequestBody String data) {
    return modelService.breakSentences(data);
  }

//  @POST
//  @Path("/follow22")
//  public Uni<Void> train(@RequestBody String data) {
//    return categorizerService.trainModel();
//  }
}

package io.github.jelilio.smbackend.botmanager.web.rest;

import io.github.jelilio.smbackend.botmanager.entity.Model;
import io.github.jelilio.smbackend.botmanager.entity.enumeration.ModelType;
import io.github.jelilio.smbackend.botmanager.service.CategorizerService;
import io.github.jelilio.smbackend.botmanager.service.ModelService;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.entity.enumeration.Language;
import io.github.jelilio.smbackend.common.utils.PageRequest;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@WithSession
@RequestScoped
@RolesAllowed("ROLE_ADMIN")
@Path("/api/models")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ModelResource {
  private static final Logger logger = LoggerFactory.getLogger(ModelResource.class);
  @Inject
  ModelService modelService;

  @Inject
  CategorizerService categorizerService;

  @GET
  @Path("/all")
  public Uni<List<Model>> getAll(@QueryParam("type") ModelType type, @QueryParam("lang") Language language) {
    logger.debug("etAll: type: {}, language: {}", type, language);
    return modelService.findAll(type, language);
  }

  @GET
  @Path("/{id}")
  public Uni<Model> findOne(@PathParam("id") String id, @QueryParam("type") ModelType type) {
    logger.debug("findOne: type: {}", type);
    return modelService.findByIdAndType(id, type);
  }

  @GET
  @Path("")
  public Uni<Paged<Model>> get(@QueryParam("type") ModelType type, @BeanParam PageRequest pageRequest) {
    logger.debug("get: type: {}", type);
    return modelService.findAll(type, pageRequest.size, pageRequest.page);
  }

  @POST
  @Path("/follow")
  public Uni<List<String>> breakThis(@RequestBody String data) {
    return modelService.breakSentences(data);
  }

  @POST
  @Path("/follow22")
  public Uni<Void> train(@RequestBody String data) {
    return categorizerService.trainModel();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public void multipart(@RestForm String description,
                        @RestForm("file") FileUpload file,
                        @RestForm @PartType(MediaType.APPLICATION_JSON) RegisterRes person) {
    // do something
    logger.info("file: {}, res: {}, desc: {}", file.name(), person, description);
  }
}

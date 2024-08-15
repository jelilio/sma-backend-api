package io.github.jelilio.smbackend.usermanager.client.cloudinary;


import io.github.jelilio.smbackend.common.dto.response.CloudinaryRes;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;

@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "cloudinary-api")
public interface CloudinaryProxy {

  @POST
  @Path("")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Uni<CloudinaryRes> sendMultipartData(
      @RestForm("file") File data,
      @RestForm("api_key") String key,
      @RestForm("upload_preset") String uploadPreset
  );
}


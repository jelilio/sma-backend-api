package io.github.jelilio.smbackend.newsfeed.client;


import io.github.jelilio.smbackend.common.dto.response.ImgurResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;

@Path("/image")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "imgur-api")
@ClientHeaderParam(name = "Authorization", value = "${app.imgur.key}")
public interface ImgurProxy {

  @POST
  @Path("")
  Uni<ImgurResponse> sendMultipartData(@RestForm("image") File data);
}


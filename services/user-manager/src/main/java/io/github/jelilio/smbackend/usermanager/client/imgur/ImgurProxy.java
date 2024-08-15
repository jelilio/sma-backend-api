package io.github.jelilio.smbackend.usermanager.client.imgur;

import io.github.jelilio.smbackend.common.dto.MultipartBody;
import io.github.jelilio.smbackend.common.dto.response.ImgurResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/image")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "imgur-api")
@ClientHeaderParam(name = "Authorization", value = "${app.imgur.key}")
public interface ImgurProxy {
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Uni<ImgurResponse> sendMultipartData(@BeanParam MultipartBody data);
}

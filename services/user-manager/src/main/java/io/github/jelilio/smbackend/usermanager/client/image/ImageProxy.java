package io.github.jelilio.smbackend.usermanager.client.image;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("")
public interface ImageProxy {
  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Uni<Response> stream();
}

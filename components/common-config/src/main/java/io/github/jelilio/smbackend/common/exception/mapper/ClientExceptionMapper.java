package io.github.jelilio.smbackend.common.exception.mapper;


import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@Provider
public class ClientExceptionMapper implements ExceptionMapper<ClientWebApplicationException> {
  @Override
  public Response toResponse(ClientWebApplicationException t) {
    return Response
        .status(t.getResponse().getStatus())
        .entity(t.getResponse().getEntity())
        .build();
  }
}

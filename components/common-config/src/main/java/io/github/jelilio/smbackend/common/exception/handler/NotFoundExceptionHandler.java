package io.github.jelilio.smbackend.common.exception.handler;


import io.github.jelilio.smbackend.common.exception.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundException> {
  @Override
  public Response toResponse(NotFoundException ex) {
    return Response.status(Response.Status.NOT_FOUND).
        entity(Map.of("message", ex.getMessage())).build();
  }
}

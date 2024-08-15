package io.github.jelilio.smbackend.botmanager.exception.handler;


import io.github.jelilio.smbackend.botmanager.exception.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class BadRequestExceptionHandler implements ExceptionMapper<BadRequestException> {
  @Override
  public Response toResponse(BadRequestException ex) {
    return Response.status(Response.Status.BAD_REQUEST).
        entity(Map.of("message", ex.getMessage())).build();
  }
}

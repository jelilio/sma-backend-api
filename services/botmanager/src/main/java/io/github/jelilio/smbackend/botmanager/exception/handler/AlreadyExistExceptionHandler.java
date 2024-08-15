package io.github.jelilio.smbackend.botmanager.exception.handler;


import io.github.jelilio.smbackend.botmanager.exception.AlreadyExistException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class AlreadyExistExceptionHandler implements ExceptionMapper<AlreadyExistException> {
  @Override
  public Response toResponse(AlreadyExistException ex) {
    return Response.status(Response.Status.NOT_FOUND).
        entity(Map.of("message", ex.getMessage())).build();
  }
}

package io.github.jelilio.smbackend.common.exception.handler;

import io.github.jelilio.smbackend.common.exception.AuthenticationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class AuthenticationExceptionHandler implements ExceptionMapper<AuthenticationException> {
  @Override
  public Response toResponse(AuthenticationException ex) {
    return Response.status(Response.Status.BAD_REQUEST).
        entity(Map.of("message", ex.getMessage(), "code", ex.getCode())).build();
  }
}

package io.github.jelilio.smbackend.common.exception.mapper;


import io.github.jelilio.smbackend.common.exception.BadRequestException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class BadRequestExceptionMapper implements ResponseExceptionMapper<BadRequestException> {
  @Override
  public BadRequestException toThrowable(Response response) {
    if(response.getStatus() == 400) {
      try {
        return response.readEntity(BadRequestException.class);
      } catch (ProcessingException ex) {
        return new BadRequestException(response.readEntity(String.class));
      }
    }
    return null;
  }
}

package io.github.jelilio.smbackend.botmanager.exception.mapper;


import io.github.jelilio.smbackend.botmanager.exception.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class NotFoundExceptionMapper implements ResponseExceptionMapper<NotFoundException> {
  private static final Logger logger = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

  @Override
  public NotFoundException toThrowable(Response response) {
    if(response.getStatus() == 404) {
      try {
        return response.readEntity(NotFoundException.class);
      } catch (ProcessingException ex) {
        return new NotFoundException(response.readEntity(String.class));
      }
    }
    return null;
  }
}

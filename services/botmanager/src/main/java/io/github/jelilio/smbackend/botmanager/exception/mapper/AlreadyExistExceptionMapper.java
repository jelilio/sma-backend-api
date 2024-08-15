package io.github.jelilio.smbackend.botmanager.exception.mapper;


import io.github.jelilio.smbackend.botmanager.exception.AlreadyExistException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class AlreadyExistExceptionMapper implements ResponseExceptionMapper<AlreadyExistException> {
  private static final Logger logger = LoggerFactory.getLogger(AlreadyExistExceptionMapper.class);

  @Override
  public AlreadyExistException toThrowable(Response response) {
    if(response.getStatus() == 404) {
      try {
        return response.readEntity(AlreadyExistException.class);
      } catch (ProcessingException ex) {
        return new AlreadyExistException(response.readEntity(String.class));
      }
    }
    return null;
  }
}

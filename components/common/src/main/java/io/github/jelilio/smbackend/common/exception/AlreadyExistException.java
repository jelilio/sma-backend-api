package io.github.jelilio.smbackend.common.exception;

public class AlreadyExistException extends RuntimeException {
  public AlreadyExistException(String message) {
    super(message);
  }
}

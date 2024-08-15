package io.github.jelilio.smbackend.botmanager.utils;

import io.github.jelilio.smbackend.botmanager.exception.NotFoundException;
import opennlp.tools.util.InputStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StringInputStreamFactory implements InputStreamFactory  {
  private final String content;

  public StringInputStreamFactory(String file) throws NotFoundException {
    if (file.isEmpty()) {
      throw new NotFoundException("Content '" + file + "' cannot be empty");
    }
    this.content = file;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }
}

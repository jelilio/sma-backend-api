package io.github.jelilio.smbackend.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class FileUtil {
  public static String encodeFileToBase64(File file) throws IOException {
    byte[] fileContent = Files.readAllBytes(file.toPath());
    return Base64.getEncoder().encodeToString(fileContent);
  }
}

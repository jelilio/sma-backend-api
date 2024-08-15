package io.github.jelilio.smbackend.common.dto;

import io.github.jelilio.smbackend.common.utils.FileUtil;
import jakarta.ws.rs.FormParam;

import java.io.File;
import java.io.IOException;

public class MultipartBody {
  @FormParam("image")
  public String image;

  public MultipartBody() {}

  public MultipartBody(String image) {
    this.image = image;
  }

  public MultipartBody(File file) throws IOException {
    this.image = FileUtil.encodeFileToBase64(file);
  }
}

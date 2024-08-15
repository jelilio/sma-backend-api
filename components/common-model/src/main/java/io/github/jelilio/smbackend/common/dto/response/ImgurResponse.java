package io.github.jelilio.smbackend.common.dto.response;

public class ImgurResponse {
  public int status;
  public ImgurData data;
  public boolean success;

  @Override
  public String toString() {
    return "ImgurResponse{" +
        "status=" + status +
        ", data=" + data +
        ", success=" + success +
        '}';
  }
}

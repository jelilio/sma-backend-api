package io.github.jelilio.smbackend.common.dto.response;

public record ImgurData(
    String id,
    String link,
    String type,
    boolean animated,
    int width,
    int height,
    long size
){
  @Override
  public String toString() {
    return "ImgurData{" +
        "id='" + id + '\'' +
        ", link='" + link + '\'' +
        ", type='" + type + '\'' +
        ", animated=" + animated +
        ", width=" + width +
        ", height=" + height +
        ", size=" + size +
        '}';
  }
}

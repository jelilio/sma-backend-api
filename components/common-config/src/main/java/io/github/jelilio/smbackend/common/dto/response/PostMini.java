package io.github.jelilio.smbackend.common.dto.response;

import java.util.UUID;

public class PostMini {
  public UUID id;
  public String caption;
  public String imageUrl;
  public String imageType;

  public PostMini() {}

  public PostMini(UUID id, String caption, String imageUrl, String imageType) {
    this.id = id;
    this.caption = caption;
    this.imageUrl = imageUrl;
    this.imageType = imageType;
  }
}

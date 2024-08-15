package io.github.jelilio.smbackend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CloudinaryRes (
    @JsonProperty("url") String url,
    @JsonProperty("format") String format,
    @JsonProperty("resource_type") String resourceType,
    @JsonProperty("secure_url") String secureUrl
) {
  public String getType() {
    return String.format("%s/%s", resourceType, format);
  }
}

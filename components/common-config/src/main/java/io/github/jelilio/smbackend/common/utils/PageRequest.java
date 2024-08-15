package io.github.jelilio.smbackend.common.utils;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

public class PageRequest {
  @QueryParam("page")
  @DefaultValue("0")
  public int page;
  @QueryParam("size")
  @DefaultValue("20")
  public int size;

  @Override
  public String toString() {
    return "PageRequest{" +
        "page=" + page +
        ", size=" + size +
        '}';
  }
}

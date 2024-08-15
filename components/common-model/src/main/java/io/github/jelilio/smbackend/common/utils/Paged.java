package io.github.jelilio.smbackend.common.utils;

import java.util.List;

public class Paged<T> {
  public List<T> content;
  public MetaFields meta = new MetaFields();

  public static <T> Paged<T> empty() {
    return new Paged<>(new Page(20), 0, 0, List.of());
  }

  public Paged() {}

  public Paged(Page page, int totalPages, long totalElements, List<T> content) {
    this.content = content;
    this.meta.size = page.size;
    this.meta.page = page.index;
    this.meta.numberOfElements = content.size();
    this.meta.totalPages = totalPages;
    this.meta.totalElements = totalElements;
  }

  public Paged(MetaFields meta, List<T> content) {
    this.meta = meta;
    this.content = content;
  }

  public static class MetaFields {
    public int size;
    public int page;
    public int numberOfElements;
    public int totalPages;
    public long totalElements;

    @Override
    public String toString() {
      return "MetaFields{" +
          "size=" + size +
          ", page=" + page +
          ", numberOfElements=" + numberOfElements +
          ", totalPages=" + totalPages +
          ", totalElements=" + totalElements +
          '}';
    }
  }

  @Override
  public String toString() {
    return "Paged{" +
        "content=" + content +
        ", meta=" + meta +
        '}';
  }
}

package io.github.jelilio.smbackend.botmanager.utils;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class PaginationUtil {
  private static final Logger logger = LoggerFactory.getLogger(PaginationUtil.class);

  public static <T> Paged<T> paginate(Page page, PanacheQuery<T> query) {
    List<T> content = query.list();
    int totalPages = query.pageCount();
    long totalElements = query.count();

    return new Paged<>(page, totalPages, totalElements, content);
  }
}

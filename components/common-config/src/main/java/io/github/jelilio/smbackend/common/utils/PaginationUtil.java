package io.github.jelilio.smbackend.common.utils;

import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class PaginationUtil {
  private static final Logger logger = LoggerFactory.getLogger(PaginationUtil.class);

  public static <T> Uni<Paged<T>> paginate(Page page, PanacheQuery<T> query) {
    Uni<List<T>> uniContents = query.list();
    Uni<Integer> uniTotalPages = query.pageCount();
    Uni<Long> uniTotalElements = query.count();

    return uniContents.flatMap(content ->
        uniTotalPages.flatMap(totalPages -> {
          logger.info("totalPages: {}", totalPages);
          return uniTotalElements.map(totalElements -> {
            logger.info("totalElements: {}", totalElements);
            return new Paged<>(page, totalPages, totalElements, content);
          });
        })
    );
  }

  public static <T> Uni<Paged<T>> paginate(Page page, PanacheQuery<T> query, Uni<Long> uniCounts) {
    Uni<List<T>> uniContents = query.list();

    return paginate(page, uniContents, uniCounts);
  }

  public static <T> Uni<Paged<T>> paginate(Page page, Uni<List<T>> uniContents, Uni<Long> uniCounts) {

    return uniContents.flatMap(content -> {
      return uniCounts.map(totalElements -> {
        int totalPages = (int)Math.ceil((float) totalElements / page.size);

        return new Paged<>(page, totalPages, totalElements, content);
      });
    });
  }
}

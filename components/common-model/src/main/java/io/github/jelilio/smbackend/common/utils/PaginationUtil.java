package io.github.jelilio.smbackend.common.utils;

import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class PaginationUtil {
  private static final Logger logger = LoggerFactory.getLogger(PaginationUtil.class);

  public static <T> Uni<Paged<T>> paginate(Page page, Uni<List<T>> uniContents, Uni<Integer> uniTotalPages, Uni<Long> uniTotalElements) {
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

  public static <T> Uni<Paged<T>> paginate(Page page, Uni<List<T>> uniContents, Uni<Long> uniCounts) {

    return uniContents.flatMap(content -> {
      return uniCounts.map(totalElements -> {
        int totalPages = (int)Math.ceil((float) totalElements / page.size);

        return new Paged<>(page, totalPages, totalElements, content);
      });
    });
  }
}

package io.github.jelilio.smbackend.newsfeed.service;

import io.github.jelilio.smbackend.common.entity.enumeration.DateQueryType;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.dto.response.AnalysedObject;
import io.github.jelilio.smbackend.newsfeed.entity.Violation;
import io.smallrye.mutiny.Uni;

import java.time.Instant;
import java.util.List;

public interface ViolationService {
  Uni<Paged<Violation>> findAll(int size, int index);

  Uni<Paged<Violation>> findAll(Instant startDate, Instant endDate, int size, int index);

  Uni<Paged<Violation>> findAll(String userId, int size, int index);

  Uni<List<Violation>> findAllByQueryType(DateQueryType queryType);

  Uni<Paged<Violation>> findAllByQueryType(DateQueryType queryType, int size, int index);

  Uni<Void> createAndExecute(AnalysedObject analysedObject);

  Uni<Void> userSuspended(String userId, Boolean status);
}

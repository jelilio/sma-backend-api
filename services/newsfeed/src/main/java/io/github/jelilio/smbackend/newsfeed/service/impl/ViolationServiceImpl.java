package io.github.jelilio.smbackend.newsfeed.service.impl;

import io.github.jelilio.smbackend.common.entity.enumeration.DateQueryType;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.commonutil.dto.response.AnalysedObject;
import io.github.jelilio.smbackend.commonutil.dto.response.PostObject;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.Violation;
import io.github.jelilio.smbackend.newsfeed.service.MailerService;
import io.github.jelilio.smbackend.newsfeed.service.PostService;
import io.github.jelilio.smbackend.newsfeed.service.UserService;
import io.github.jelilio.smbackend.newsfeed.service.ViolationService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ViolationServiceImpl implements ViolationService {
  private static final Logger logger = LoggerFactory.getLogger(ViolationServiceImpl.class);

  private static final Map<DateQueryType, Integer> dateQueryTypeMap = Map.of(
      DateQueryType.LAST7DAYS, 7,
      DateQueryType.LAST30DAYS, 30,
      DateQueryType.LAST90DAYS, 90,
      DateQueryType.ALL, -1
  );

  @Inject
  PostService postService;

  @Inject
  UserService userService;

  @Inject
  MailerService mailerService;

  @Channel("user-suspension")
  Emitter<String> userSuspension;

  @Override
  public Uni<Paged<Violation>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Violation.findAll().page(page));
  }

  @Override
  public Uni<Paged<Violation>> findAll(Instant startDate, Instant endDate, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Violation.find(startDate, endDate).page(page));
  }

  @Override
  public Uni<Paged<Violation>> findAll(String userId, int size, int index) {
    Page page = Page.of(index, size);

    return userService.findById(userId).flatMap(user -> {
      return PaginationUtil.paginate(page, Violation.find(user).page(page));
    });
  }

  @Override
  public Uni<Paged<Violation>> findAllByQueryType(DateQueryType queryType, int size, int index) {
    Page page = Page.of(index, size);

    Instant startDate = null;

    int lastDays = dateQueryTypeMap.getOrDefault(queryType, -1);

    if(lastDays > 0) {
      startDate = ZonedDateTime.now().minusDays(lastDays).toInstant();
    }

    if (startDate == null) return findAll(size, index);

    return PaginationUtil.paginate(page, Violation.findAllByCreatedAfterDate(startDate).page(page));
  }

  @Override
  public Uni<List<Violation>> findAllByQueryType(DateQueryType queryType) {
    Instant startDate = null;

    int lastDays = dateQueryTypeMap.getOrDefault(queryType, -1);

    if(lastDays > 0) {
      startDate = ZonedDateTime.now().minusDays(lastDays).toInstant();
    }

    if (startDate == null) return Violation.findAll().list();

    return Violation.findAllByCreatedAfterDate(startDate).list();
  }

  @Override
  public Uni<Void> createAndExecute(AnalysedObject analysedObject) {
    PostObject postObject = analysedObject.post();
    var results = analysedObject.results();
    Uni<Post> postUni = postService.findByIdOrNull(postObject.id());
    Uni<User> ownerUni = userService.findByIdOrNull(postObject.ownerId());

    return Panache.withTransaction(() -> {
      return postUni.flatMap(post -> {
        return ownerUni.flatMap(owner -> {
          List<Violation> violations = new ArrayList<>();
          Multi<Violation> multiViolations = Multi.createFrom()
              .iterable(results).map(it -> {
            return new Violation(it.left(), it.middle(), it.right(),
                postObject.caption(), postObject.imageUrl(), owner, post);
          });

          return multiViolations.collect().asList().flatMap(exViolations -> {
            logger.info("total violations: {}", violations.size());
            return Violation.persist(violations)
                .flatMap(__ -> {
                  if(violations.isEmpty()) return Uni.createFrom().voidItem();

                  return executeViolation(exViolations.get(0));
                });
          });
        });
      });
    });
  }

  public Uni<Void> executeViolation(Violation violation) {
    switch (violation.action) {
      case WARN_USER -> {
        return mailerService.sendSuspensionMail(violation.owner)
            .flatMap(__ -> Uni.createFrom().voidItem());
      }
      case DELETE_POST -> {
        logger.info("delete post: {}", violation.post.id);
        return postService.deleteByPost(violation.owner.id.toString(), violation.post.id.toString())
            .flatMap(__ -> Uni.createFrom().voidItem());
      }
      case SUSPEND_USER -> {
        logger.info("sending suspend user: {}", violation.owner.id.toString());
        userSuspension.send(violation.owner.id.toString());
        return Uni.createFrom().voidItem();
      }
      default -> {
        return Uni.createFrom().voidItem();
      }
    }
  }

  @Override
  public Uni<Void> userSuspended(String userId, Boolean status) {
    logger.info("userSuspended: {}, {}", userId, status);
    if(!status) return Uni.createFrom().voidItem();

    Uni<User> userUni = userService.findById(userId);

    return userUni.flatMap(extUser -> {
      if(extUser.suspendedDate != null) { //  already suspended
        return Uni.createFrom().voidItem();
      }

      extUser.enabled = false;
      extUser.suspendedDate = Instant.now();
      return Panache.<User>withTransaction(extUser::persist)
          .flatMap(updated -> mailerService.sendSuspensionMail(updated))
          .flatMap(__ -> Uni.createFrom().voidItem());
    });
  }
}

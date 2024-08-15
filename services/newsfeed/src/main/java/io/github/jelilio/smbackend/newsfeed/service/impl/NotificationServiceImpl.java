package io.github.jelilio.smbackend.newsfeed.service.impl;

import io.github.jelilio.smbackend.common.entity.enumeration.DateQueryType;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.NotificationType;
import io.github.jelilio.smbackend.commonutil.utils.LocaleMessageUtil;
import io.github.jelilio.smbackend.newsfeed.entity.Notification;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.entity.Violation;
import io.github.jelilio.smbackend.newsfeed.service.NotificationService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class NotificationServiceImpl implements NotificationService {
  private static final Map<DateQueryType, Integer> userQueryTypeMap = Map.of(
      DateQueryType.LAST7DAYS, 7,
      DateQueryType.LAST30DAYS, 30,
      DateQueryType.LAST90DAYS, 90,
      DateQueryType.ALL, -1
  );

  @Override
  public Uni<Paged<Notification>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Violation.findAll().page(page));
  }

  @Override
  public Uni<Paged<Notification>> findAll(User owner, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Notification.find(owner).page(page));
  }

  @Override
  public Uni<Notification> createNotification(NotificationType type, User loggedInUser, User otherUser) {
    String caption = LocaleMessageUtil.getDefaultMessage(String.format("notification.caption.%s", type.name()), loggedInUser.name);

    return Panache.withTransaction(() -> {
      var notification = new Notification(caption, type, otherUser, loggedInUser);

      return Panache.withTransaction(notification::persist);
    });
  }

  @Override
  public Uni<Notification> createNotification(NotificationType type, User loggedInUser, Post post) {
    String caption = LocaleMessageUtil.getDefaultMessage(String.format("notification.caption.%s", type.name()), loggedInUser.name);

    return Panache.withTransaction(() -> {
      var notification = new Notification(caption, type, loggedInUser, post, post.owner);

      return Panache.withTransaction(notification::persist);
    });
  }
}

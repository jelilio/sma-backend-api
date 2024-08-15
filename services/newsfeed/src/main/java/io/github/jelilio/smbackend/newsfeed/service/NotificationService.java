package io.github.jelilio.smbackend.newsfeed.service;

import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.NotificationType;
import io.github.jelilio.smbackend.newsfeed.entity.Notification;
import io.github.jelilio.smbackend.newsfeed.entity.Post;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.smallrye.mutiny.Uni;

public interface NotificationService {
  Uni<Paged<Notification>> findAll(int size, int index);

  Uni<Paged<Notification>> findAll(User owner, int size, int index);

  Uni<Notification> createNotification(NotificationType type, User loggedInUser, User otherUser);

  Uni<Notification> createNotification(NotificationType type, User loggedInUser, Post post);
}

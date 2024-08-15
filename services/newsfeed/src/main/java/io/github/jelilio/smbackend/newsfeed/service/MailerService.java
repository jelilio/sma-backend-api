package io.github.jelilio.smbackend.newsfeed.service;

import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.smallrye.mutiny.Uni;

public interface MailerService {
  Uni<Void> sendWarningMail(User login);

  Uni<Void> sendSuspensionMail(User login);

  Uni<Void> sendDeletePostMail(User user);
}

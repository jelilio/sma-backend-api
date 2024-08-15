package io.github.jelilio.smbackend.newsfeed.service.impl;

import io.github.jelilio.smbackend.commonutil.utils.LocaleMessageUtil;
import io.github.jelilio.smbackend.newsfeed.entity.User;
import io.github.jelilio.smbackend.newsfeed.service.MailerService;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MailerServiceImpl implements MailerService {
  private static final Logger logger = LoggerFactory.getLogger(MailerServiceImpl.class);

  @Inject
  ReactiveMailer reactiveMailer;

  @Inject
  @Location("mails/warning")
  MailTemplate warning;

  @Inject
  @Location("mails/suspension")
  MailTemplate suspension;

  @ConfigProperty(name = "application.name")
  String applicationName;

  @Override
  public Uni<Void> sendWarningMail(User user) {
    logger.info("mail-service: sending sendWarningMail: login: {}", user.email);
    return suspension
        .to(user.email)
        .subject(LocaleMessageUtil.getDefaultMessage("mail.subject.warning"))
        .data("name", user.name)
        .data("applicationName", applicationName)
        .send();
  }

  @Override
  public Uni<Void> sendSuspensionMail(User user) {
    logger.info("mail-service: sending sendSuspensionMail: login: {}", user.email);
    return suspension
        .to(user.email)
        .subject(LocaleMessageUtil.getDefaultMessage("mail.subject.suspension"))
        .data("name", user.name)
        .data("applicationName", applicationName)
        .send();
  }

  @Override
  public Uni<Void> sendDeletePostMail(User user) {
    logger.info("mail-service: sending sendDeletePostMail: login: {}", user.email);
    return Uni.createFrom().voidItem();
  }
}

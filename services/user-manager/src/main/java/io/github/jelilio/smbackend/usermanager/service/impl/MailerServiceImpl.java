package io.github.jelilio.smbackend.usermanager.service.impl;

import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.commonutil.utils.LocaleMessageUtil;
import io.github.jelilio.smbackend.usermanager.entity.User;
import io.github.jelilio.smbackend.usermanager.service.MailerService;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MailerServiceImpl implements MailerService {
  @Inject
  ReactiveMailer reactiveMailer;

  @Inject
  @Location("mails/verification")
  MailTemplate otpVerification;

  @Inject
  @Location("mails/welcome")
  MailTemplate welcome;

  @ConfigProperty(name = "application.name")
  String applicationName;

  private static final Logger logger = LoggerFactory.getLogger(MailerServiceImpl.class);

  @Override
  public Uni<Void> sendOtpMail(RegisterRes user, String otpKey, Long otpKeyDuration) {
    logger.debug("mail-service: sending sendOtpMail: otp: {}, user: {}", otpKey, user.email());
    return otpVerification
        .to(user.email())
        .subject(LocaleMessageUtil.getDefaultMessage("mail.subject.otp-verification", otpKey))
        .data("otpKey", otpKey)
        .data("applicationName", applicationName)
        .data("expireIn", TimeUnit.SECONDS.toMinutes(otpKeyDuration))
        .send();
  }

  @Override
  public Uni<Void> sendPasswordResetMail(User user, String otpKey, Long otpKeyDuration) {
    logger.debug("mail-service: sending sendPasswordResetMail: otp: {}, user: {}", otpKey, user.email);
    return Uni.createFrom().voidItem();
  }

  @Override
  public Uni<Void> sendWelcomeEmail(User user) {
    logger.debug("mail-service: sending sendWelcomeEmail to: {}", user.email);
    return welcome
        .to(user.email)
        .subject(LocaleMessageUtil.getDefaultMessage("mail.subject.welcome", applicationName))
        .data("name", user.name)
        .data("applicationName", applicationName)
        .send();
  }
}

package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.usermanager.entity.User;
import io.smallrye.mutiny.Uni;

public interface MailerService {
  Uni<Void> sendOtpMail(RegisterRes user, String otpKey, Long otpKeyDuration);

  Uni<Void> sendPasswordResetMail(User login, String otpKey, Long otpKeyDuration);

  Uni<Void> sendWelcomeEmail(User user);
}

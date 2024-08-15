package io.github.jelilio.smbackend.usermanager.queue.processor;

import io.github.jelilio.smbackend.common.dto.response.UserSus;
import io.github.jelilio.smbackend.usermanager.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WithSession
@ApplicationScoped
public class UserSuspension {
  private static final Logger logger = LoggerFactory.getLogger(UserSuspension.class);

  @Inject
  UserService userService;

  @NonBlocking
  @Incoming("user-suspension")
  @Outgoing("user-suspended")
  @ActivateRequestContext
  public Uni<UserSus> process(Message<String> object) throws InterruptedException {
    logger.info("user-suspension: user's to suspend: {}", object);

    // simulate some hard-working task
    String userOidcId = object.getPayload();
    logger.info("user's to suspend: {}", userOidcId);
    return userService.suspendUser(userOidcId)
        .map(status -> new UserSus(userOidcId, status))
        .invoke(__ -> object.ack());
  }
}

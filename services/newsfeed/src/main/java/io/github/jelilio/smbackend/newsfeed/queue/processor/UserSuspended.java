package io.github.jelilio.smbackend.newsfeed.queue.processor;

import io.github.jelilio.smbackend.common.dto.response.UserSus;
import io.github.jelilio.smbackend.newsfeed.service.ViolationService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WithSession
@ApplicationScoped
public class UserSuspended {
  private static final Logger logger = LoggerFactory.getLogger(UserSuspended.class);

  @Inject
  ViolationService violationService;

  @NonBlocking
  @Incoming("user-suspended")
  @ActivateRequestContext
  public Uni<Void> process(Message<JsonObject> object) {
    logger.info("user-suspended: user suspended {}", object);

    UserSus register = object.getPayload().mapTo(UserSus.class);
    return violationService.userSuspended(register.oidcId(), register.status())
        .flatMap(it -> Uni.createFrom().voidItem())
        .invoke(__ -> object.ack());
  }
}

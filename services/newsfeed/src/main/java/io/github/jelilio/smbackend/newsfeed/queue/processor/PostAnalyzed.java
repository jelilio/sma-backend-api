package io.github.jelilio.smbackend.newsfeed.queue.processor;

import io.github.jelilio.smbackend.commonutil.dto.response.AnalysedObject;
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
public class PostAnalyzed {
  private static final Logger logger = LoggerFactory.getLogger(PostAnalyzed.class);

  @Inject
  ViolationService validationService;

  @NonBlocking
  @Incoming("post-analyzed")
  @ActivateRequestContext
  public Uni<Void> process(Message<JsonObject> object) {
    logger.info("registered user's payload received");

    AnalysedObject register = object.getPayload().mapTo(AnalysedObject.class);
    return validationService.createAndExecute(register)
        .flatMap(it -> Uni.createFrom().voidItem())
        .invoke(__ -> object.ack());
  }
}

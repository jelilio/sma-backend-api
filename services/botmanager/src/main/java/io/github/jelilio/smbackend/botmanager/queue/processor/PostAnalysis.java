package io.github.jelilio.smbackend.botmanager.queue.processor;

import io.github.jelilio.smbackend.botmanager.service.BotService;
import io.github.jelilio.smbackend.commonutil.dto.response.AnalysedObject;
import io.github.jelilio.smbackend.commonutil.dto.response.PostObject;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PostAnalysis {
  private static final Logger logger = LoggerFactory.getLogger(PostAnalysis.class);

  @Inject
  BotService botService;

  @Blocking
  @Transactional
  @Incoming("post-analysis")
  @Outgoing("post-analyzed")
  public AnalysedObject process(Message<JsonObject> object) throws InterruptedException {
    logger.info("user's post payload received: {}", object);

    // simulate some hard-working task
    PostObject post = object.getPayload().mapTo(PostObject.class);
    logger.info("user's post payload received: {}", post);
    return botService.analyzePost(post);
  }
}

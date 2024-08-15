package io.github.jelilio.smbackend.usermanager.queue.processor;

import io.github.jelilio.smbackend.common.dto.RegisterDto;
import io.github.jelilio.smbackend.common.dto.response.RegisterRes;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;
import io.github.jelilio.smbackend.usermanager.queue.model.RegisterUser;
import io.github.jelilio.smbackend.usermanager.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@WithSession
@ApplicationScoped
public class UserUploadProcessor {
  private static final Logger logger = LoggerFactory.getLogger(UserUploadProcessor.class);

  @Inject
  UserService userService;

  @Channel("user-registered")
  Emitter<RegisterRes> userRegister;

  @NonBlocking
  @Incoming("user-oidc-register")
  @ActivateRequestContext
  public Uni<Void> process(Message<JsonObject> object) {
    logger.info("send user ot OIDC for registration");

    RegisterUser register = object.getPayload().mapTo(RegisterUser.class);

    String currentId = register.id();
    RegisterDto dto = register.dto();
    Set<String> roles = register.roles();
    UserType type = register.type();

    return userService.registerOnOIDC(dto, roles, type)
        .flatMap(kUser -> userService.updateId(currentId, kUser))
        .map(user -> {
          if(user == null) return null;

          var res = new RegisterRes(user.oidcId.toString(), user.name,
              user.email, user.username, user.enabled, user.type);
          userRegister.send(res);
          return res;
        })
        .flatMap(it -> Uni.createFrom().voidItem())
        .invoke(__ -> object.ack());
  }
}

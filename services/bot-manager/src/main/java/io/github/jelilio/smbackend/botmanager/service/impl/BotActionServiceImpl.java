package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.entity.key.BotActionId;
import io.github.jelilio.smbackend.botmanager.service.BotActionService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.common.dto.BotActionDto;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class BotActionServiceImpl implements BotActionService {
  private static final Logger logger = LoggerFactory.getLogger(BotActionServiceImpl.class);

  @Inject
  CategorizerItemService categorizerItemService;

  @Override
  public Uni<BotAction> findById(BotActionId id) {
    logger.info("findById: id: {}", id);
    return BotAction.findOneById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("BotAction not found"));
  }

  @Override
  public Uni<Paged<BotAction>> findAllActions(Bot bot, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, BotAction.findAllByBot(bot).page(page));
  }

  public Uni<Boolean> checkIfItExist(Bot bot, Action action, CategorizerItem item) {
    BotActionId id = new BotActionId(bot.id, item.id, action);
    return BotAction.countById(id).map(count -> count > 0);
  }

  TriFunction<Bot, Action, CategorizerItem, Uni<Boolean>> checkIfActionExist = (Bot bot, Action action, CategorizerItem item) -> {
    if (item == null) return Uni.createFrom().failure(new Exception("Categorizer item is required"));

    Uni<Boolean> uni = checkIfItExist(bot, action, item);

    return uni.flatMap(inUsed -> {
      if (inUsed) {
        return Uni.createFrom().failure(() -> new AlreadyExistException("Action with this parameters already exist"));
      }
      return Uni.createFrom().item(true);
    });
  };

  @Override
  public Uni<BotAction> create(Bot bot, BotActionDto dto) {
    Uni<CategorizerItem> uniItem = categorizerItemService.findById(dto.item().id());

    return Panache.withTransaction(() ->
        uniItem.flatMap(item -> {
          return checkIfActionExist.apply(bot, dto.action(), item)
              .flatMap(__ -> {
                BotAction botAction = new BotAction(bot, item, dto.action(), dto.description());
                return Panache.withTransaction(botAction::persist);
              });
        })
    );
  }

  @Override
  public Uni<Void> delete(Bot bot, Action action, String itemId) {
    Uni<BotAction> uniItem = findById(new BotActionId(bot.id, UUID.fromString(itemId), action));

    return Panache.withTransaction(() ->
        uniItem.flatMap(botAction -> Panache.withTransaction(botAction::delete))
    );
  }
}

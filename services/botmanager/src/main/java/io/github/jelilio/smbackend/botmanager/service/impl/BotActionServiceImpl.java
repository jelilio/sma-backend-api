package io.github.jelilio.smbackend.botmanager.service.impl;

import io.github.jelilio.smbackend.botmanager.dto.BotActionDto;
import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.botmanager.entity.CategorizerItem;
import io.github.jelilio.smbackend.botmanager.entity.key.BotActionId;
import io.github.jelilio.smbackend.botmanager.service.BotActionService;
import io.github.jelilio.smbackend.botmanager.service.CategorizerItemService;
import io.github.jelilio.smbackend.botmanager.utils.Paged;
import io.github.jelilio.smbackend.botmanager.utils.PaginationUtil;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
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
  public BotAction findById(BotActionId id) {
    logger.info("findById: id: {}", id);
    return BotAction.findOneById(id).orElseThrow(() -> new NotFoundException("BotAction not found"));
  }

  @Override
  public Paged<BotAction> findAllActions(Bot bot, int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, BotAction.findAllByBot(bot).page(page));
  }

  public Boolean checkIfItExist(Bot bot, Action action, CategorizerItem item) {
    BotActionId id = new BotActionId(bot.id, item.id, action);
    return BotAction.countById(id) > 0;
  }

  TriFunction<Bot, Action, CategorizerItem, Boolean> checkIfActionExist = (Bot bot, Action action, CategorizerItem item) -> {
    if (item == null) return null;

    return checkIfItExist(bot, action, item);
  };

  @Override
  @Transactional
  public BotAction create(Bot bot, BotActionDto dto) {
    CategorizerItem item = categorizerItemService.findById(dto.item().id());

    Boolean itExist = checkIfActionExist.apply(bot, dto.action(), item);

    if (itExist == null) {
      throw new BadRequestException("Item is required");
    }

    BotAction botAction;
    if(itExist) {
//      throw new AlreadyExistException("Bot action with id " + dto.item().id() + " already exists");
      BotActionId id = new BotActionId(bot.id, item.id, dto.action());
      botAction = findById(id);
      botAction.description = dto.description();
      botAction.severity = dto.severity();
    } else {
      botAction = new BotAction(bot, item, dto.action(), dto.severity(), dto.description());
      botAction.persist();
    }

    return botAction;
  }

  @Override
  @Transactional
  public void delete(Bot bot, Action action, String itemId) {
    BotAction extItem = findById(new BotActionId(bot.id, UUID.fromString(itemId), action));

    extItem.delete();
  }
}

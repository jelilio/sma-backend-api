package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.dto.BotActionDto;
import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.botmanager.entity.key.BotActionId;
import io.github.jelilio.smbackend.botmanager.utils.Paged;

public interface BotActionService {
  BotAction findById(BotActionId id);

  Paged<BotAction> findAllActions(Bot bot, int size, int index);

  BotAction create(Bot bot, BotActionDto dto);

  void delete(Bot bot, Action action, String itemId);
}

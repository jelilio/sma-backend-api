package io.github.jelilio.smbackend.botmanager.service;

import io.github.jelilio.smbackend.botmanager.entity.Bot;
import io.github.jelilio.smbackend.botmanager.entity.BotAction;
import io.github.jelilio.smbackend.botmanager.entity.key.BotActionId;
import io.github.jelilio.smbackend.common.dto.BotActionDto;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.smallrye.mutiny.Uni;

public interface BotActionService {
  Uni<BotAction> findById(BotActionId id);

  Uni<Paged<BotAction>> findAllActions(Bot bot, int size, int index);

  Uni<BotAction> create(Bot bot, BotActionDto dto);

  Uni<Void> delete(Bot bot, Action action, String itemId);
}

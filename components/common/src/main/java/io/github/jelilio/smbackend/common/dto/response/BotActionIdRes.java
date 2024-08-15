package io.github.jelilio.smbackend.common.dto.response;

import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;

import java.util.Objects;

public record BotActionIdRes(
    String botId,
    String itemId,
    Action action,
    String description
) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BotActionIdRes that = (BotActionIdRes) o;
    return Objects.equals(botId, that.botId) && Objects.equals(itemId, that.itemId) && action == that.action;
  }

  @Override
  public int hashCode() {
    return Objects.hash(botId, itemId, action);
  }

  public String getIdStr() {
    return String.format("%s_%s_%s", botId, itemId, action);
  }
}

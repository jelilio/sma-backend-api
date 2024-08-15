package io.github.jelilio.smbackend.botmanager.entity.key;

import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BotActionId implements Serializable  {
  @Column(name = "bot_id")
  public UUID botId;
  @Column(name = "item_id")
  public UUID itemId;
  @Enumerated(EnumType.STRING)
  @Column(name = "action", length = 100, nullable = false)
  public Action action;

  public BotActionId() {}

  public BotActionId(UUID botId, UUID itemId, Action action) {
    this.botId = botId;
    this.itemId = itemId;
    this.action = action;
  }

  public BotActionId(String botId, String itemId, Action action) {
    this.botId = UUID.fromString(botId);
    this.itemId = UUID.fromString(itemId);
    this.action = action;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BotActionId that = (BotActionId) o;
    return Objects.equals(itemId, that.itemId) && action == that.action;
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemId, action);
  }
}

package io.github.jelilio.smbackend.botmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.smbackend.botmanager.entity.key.BotActionId;
import io.github.jelilio.smbackend.common.entity.base.AbstractAuditingEntity;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.common.entity.listener.AuditingEntityListener;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Cacheable
@Table(name = "bot_actions")
@EntityListeners(AuditingEntityListener.class)
public class BotAction extends AbstractAuditingEntity {
  @Id
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public BotActionId id;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", length = 100, nullable = false, insertable = false, updatable = false)
  public Action action;

  @Column(name = "description", length = 500)
  public String description;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "bot_id", nullable = false, insertable = false, updatable = false)
  public Bot bot;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "item_id", nullable = false, insertable = false, updatable = false)
  public CategorizerItem item;

  public static Uni<Long> countById(BotActionId id) {
    return count("id = ?1 ", id);
  }

  public static Uni<BotAction> findOneById(BotActionId id) {
    return find("id = ?1 ", id).singleResult();
  }

  public static PanacheQuery<BotAction> findAllByBot(Bot bot) {
    return find("bot = ?1", bot);
  }

  public BotAction() {}

  public BotAction(Bot bot, CategorizerItem item, Action action, String description) {
    this.id = new BotActionId(bot.id, item.id, action);
    this.bot = bot;
    this.item = item;
    this.action = action;
    this.description = description;
  }
}

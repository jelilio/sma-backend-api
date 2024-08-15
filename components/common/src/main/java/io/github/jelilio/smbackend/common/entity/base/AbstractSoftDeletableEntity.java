package io.github.jelilio.smbackend.common.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
//@FilterDefs(value = {
//    @FilterDef(name = "nonDeletedEntries", defaultCondition = "deleted_at IS NULL"),
//    @FilterDef(name = "deletedEntries", defaultCondition = "deleted_at IS NOT NULL"),
//    @FilterDef(name = "allEntries", defaultCondition = "deleted_at IS NULL or deleted_at IS NOT NULL")
//})
//@Filters({
//    @Filter(name = "nonDeletedEntries", condition = "deleted_at IS NULL")
//})
public abstract class AbstractSoftDeletableEntity extends AbstractAuditingEntity {
  @Column(name = "deleted_at")
  public Instant deletedAt;

  /**=\\[
   *
  public static Uni<Long> count(String query, Object... params) {
    return AbstractAuditingEntity.find(query, params)
        .filter("nonDeletedEntries")
        .count();
  }
   */

//  public static <T extends PanacheEntityBase> PanacheQuery<T> find(String query, Object... params) {
//    return PanacheEntityBase.find(query, params);
//  }
}

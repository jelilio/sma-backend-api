package io.github.jelilio.smbackend.botmanager.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;

import java.time.Instant;

@MappedSuperclass
@FilterDefs(value = {
    @FilterDef(name = "nonDeletedEntries", defaultCondition = "deleted_at IS NULL"),
    @FilterDef(name = "deletedEntries", defaultCondition = "deleted_at IS NOT NULL"),
    @FilterDef(name = "allEntries", defaultCondition = "deleted_at IS NULL or deleted_at IS NOT NULL")
})
@Filters({
    @Filter(name = "nonDeletedEntries", condition = "deleted_at IS NULL")
})
public abstract class AbstractSoftDeletableEntity extends AbstractAuditingEntity {
  @Column(name = "deleted_at")
  public Instant deletedAt;
}

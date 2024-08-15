package io.github.jelilio.smbackend.botmanager.entity.base;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@RequestScoped
public class AuditingEntityListener {
  private static final String SYSTEM = "system";

  @Inject
  CurrentIdentityAssociation identityAssociation;

  @PrePersist
  public void prePersist(AbstractAuditingEntity entity) {
    if(identityAssociation != null) {
      identityAssociation.getDeferredIdentity().map(it -> {
        var name = it.getPrincipal().getName();
        entity.createdBy =  name;
        entity.lastModifiedBy = name;
        return it;
      }).subscribe().asCompletionStage();
    } else {
      entity.createdBy = SYSTEM;
      entity.lastModifiedBy = SYSTEM;
    }

    entity.createdDate = Instant.now();
    entity.lastModifiedDate = Instant.now();
  }

  @PreUpdate
  public void preUpdate(AbstractAuditingEntity entity) {
    if(identityAssociation != null) {
      identityAssociation.getDeferredIdentity().map(it -> {
        entity.lastModifiedBy = it.getPrincipal().getName();
        return it;
      }).subscribe().asCompletionStage();
    } else {
      entity.lastModifiedBy = SYSTEM;
    }

    entity.lastModifiedDate = Instant.now();
  }
}


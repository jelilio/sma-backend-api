package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.common.dto.InstitutionReq;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface InstitutionService {
  Uni<Institution> findById(String id);

  Uni<List<Institution>> findAll();

  Uni<Paged<Institution>> findAll(int size, int index);

  Uni<Institution> create(InstitutionReq req);

  Uni<Institution> update(String id, InstitutionReq req);

  Uni<Void> delete(String id);
}

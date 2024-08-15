package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.common.dto.SchoolReq;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface SchoolService {
  Uni<School> findById(String id);

  Uni<List<School>> findAll();

  Uni<Paged<School>> findAll(int size, int index);

  Uni<School> create(SchoolReq req);

  Uni<School> findOrCreate(String name, Institution institution);

  Uni<School> update(String id, SchoolReq req);

  Uni<Void> delete(String id);
}

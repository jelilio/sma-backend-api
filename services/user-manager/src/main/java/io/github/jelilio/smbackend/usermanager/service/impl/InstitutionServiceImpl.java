package io.github.jelilio.smbackend.usermanager.service.impl;

import io.github.jelilio.smbackend.common.dto.InstitutionReq;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.repository.InstitutionRepository;
import io.github.jelilio.smbackend.usermanager.service.InstitutionService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class InstitutionServiceImpl implements InstitutionService {
  private static final Logger logger = LoggerFactory.getLogger(InstitutionServiceImpl.class);

  @Inject
  InstitutionRepository institutionRepository;

  public Uni<Boolean> checkIfNameIsUsed(String name) {
    return institutionRepository.countByName(name)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfNameIsUsed(String id, String name) {
    return institutionRepository.countByNameButNotId(id, name)
        .onItem().transform(count -> count > 0);
  }

  @Override
  public Uni<Institution> findById(String id) {
    return institutionRepository.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Institution not found"));
  }

  @Override
  public Uni<List<Institution>> findAll() {
    return Institution.findAll(Sort.by("id")).list();
  }

  @Override
  public Uni<Paged<Institution>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Institution.findAll(Sort.by("id")).page(page));
  }

  @Override
  public Uni<Institution> create(InstitutionReq req) {
    Uni<Boolean> uniNameIsUsed = checkIfNameIsUsed(req.name());

    return Panache.withTransaction(() ->
        uniNameIsUsed.flatMap(emailInUsed -> {
          if (emailInUsed) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Name already in used"));
          }

          Institution institution = new Institution(req.name(), req.description());

          return Panache.withTransaction(() -> institutionRepository.persist(institution));
        }));
  }


  @Override
  public Uni<Institution> update(String id, InstitutionReq req) {
    Uni<Institution> uniExtInstitution = findById(id);
    Uni<Boolean> uniNameIsUsed = checkIfNameIsUsed(id, req.name());

    return Panache.withTransaction(() ->
        uniNameIsUsed.flatMap(nameInUsed -> {
          if (nameInUsed) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Name already in used"));
          }

          return uniExtInstitution.flatMap(extInstitution  -> {
            extInstitution.name = req.name();
            extInstitution.description = req.description();
            return Panache.withTransaction(() -> institutionRepository.persist(extInstitution));
          });
        }));
  }

  @Override
  public Uni<Void> delete(String id) {
    return Panache.withTransaction(() ->
        findById(id).flatMap(PanacheEntityBase::delete)
    );
  }
}

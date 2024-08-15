package io.github.jelilio.smbackend.usermanager.service.impl;

import io.github.jelilio.smbackend.common.dto.SchoolReq;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.usermanager.entity.Institution;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.github.jelilio.smbackend.usermanager.repository.SchoolRepository;
import io.github.jelilio.smbackend.usermanager.service.InstitutionService;
import io.github.jelilio.smbackend.usermanager.service.SchoolService;
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
public class SchoolServiceImpl implements SchoolService {
  private static final Logger logger = LoggerFactory.getLogger(SchoolServiceImpl.class);

  @Inject
  InstitutionService institutionService;

  @Inject
  SchoolRepository schoolRepository;

  public Uni<Boolean> checkIfNameIsUsed(String name, Institution institution) {
    return schoolRepository.countByNameAndInstitution(name, institution)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfNameIsUsed(String id, String name, Institution institution) {
    return schoolRepository.countByNameAndInstitutionButNotId(id, name, institution)
        .onItem().transform(count -> count > 0);
  }

  @Override
  public Uni<School> findById(String id) {
    return schoolRepository.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("School not found"));
  }

  @Override
  public Uni<List<School>> findAll() {
    return School.findAll(Sort.by("id")).list();
  }

  @Override
  public Uni<Paged<School>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, School.findAll(Sort.by("id")).page(page));
  }

  @Override
  public Uni<School> create(SchoolReq req) {
    Uni<Institution> institutionUni = institutionService.findById(req.institution().id());

    return institutionUni.flatMap(institution -> create(req, institution));
  }

  @Override
  public Uni<School> findOrCreate(String name, Institution institution) {
    return schoolRepository.findByName(name, institution).flatMap(it -> {
      if(it != null) {
        return Uni.createFrom().item(it);
      }

      return create(new SchoolReq(name, null, null), institution);
    });
  }

  private Uni<School> create(SchoolReq req, Institution institution) {
    Uni<Boolean> uniNameIsUsed = checkIfNameIsUsed(req.name(), institution);

    return Panache.withTransaction(() ->
        uniNameIsUsed.flatMap(nameInUsed -> {

          if (nameInUsed) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Name already in used"));
          }

          School school = new School(req.name(), req.description(), institution);

          return Panache.withTransaction(() -> schoolRepository.persist(school));
        })
    );
  }


  @Override
  public Uni<School> update(String id, SchoolReq req) {
    Uni<Institution> institutionUni = institutionService.findById(req.institution().id());

    return institutionUni.flatMap(institution -> update(id, req, institution));
  }

  private Uni<School> update(String id, SchoolReq req, Institution institution) {
    Uni<School> uniExtSchool = findById(id);
    Uni<Boolean> uniNameIsUsed = checkIfNameIsUsed(id, req.name(), institution);

    return Panache.withTransaction(() ->
        uniNameIsUsed.flatMap(nameInUsed -> {
          if (nameInUsed) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Name already in used"));
          }

          return uniExtSchool.flatMap(extSchool  -> {
            extSchool.name = req.name();
            extSchool.description = req.description();
            extSchool.institution = institution;
            return Panache.withTransaction(() -> schoolRepository.persist(extSchool));
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

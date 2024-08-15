package io.github.jelilio.smbackend.usermanager.service.impl;

import io.github.jelilio.smbackend.common.dto.CourseReq;
import io.github.jelilio.smbackend.common.exception.AlreadyExistException;
import io.github.jelilio.smbackend.common.exception.NotFoundException;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.common.utils.PaginationUtil;
import io.github.jelilio.smbackend.usermanager.entity.Course;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.github.jelilio.smbackend.usermanager.repository.CourseRepository;
import io.github.jelilio.smbackend.usermanager.service.CourseService;
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
public class CourseServiceImpl implements CourseService {
  private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

  @Inject
  SchoolService schoolService;

  @Inject
  CourseRepository courseRepository;

  public Uni<Boolean> checkIfNameIsUsed(String name, School school) {
    return courseRepository.countByNameAndSchool(name, school)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Boolean> checkIfNameIsUsed(String id, String name, School school) {
    return courseRepository.countByNameAndSchoolButNotId(id, name, school)
        .onItem().transform(count -> count > 0);
  }

  @Override
  public Uni<Course> findById(String id) {
    return courseRepository.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Course not found"));
  }

  @Override
  public Uni<List<Course>> findAll() {
    return Course.findAll(Sort.by("id")).list();
  }

  @Override
  public Uni<Paged<Course>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Course.findAll(Sort.by("id")).page(page));
  }

  @Override
  public Uni<Course> create(CourseReq req) {
    Uni<School> schoolUni = schoolService.findById(req.school().id());

    return schoolUni.flatMap(school -> create(req, school));
  }

  @Override
  public Uni<Course> findOrCreate(String name, School school) {
    return courseRepository.findByName(name, school).flatMap(it -> {
      if(it != null) {
        return Uni.createFrom().item(it);
      }

      return create(new CourseReq(name, null, null), school);
    });
  }

  private Uni<Course> create(CourseReq req, School school) {
    Uni<Boolean> uniNameIsUsed = checkIfNameIsUsed(req.name(), school);

    return Panache.withTransaction(() ->
        uniNameIsUsed.flatMap(nameInUsed -> {

          if (nameInUsed) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Name already in used"));
          }

          Course course = new Course(req.name(), req.description(), school);

          return Panache.withTransaction(course::persist);
        })
    );
  }


  @Override
  public Uni<Course> update(String id, CourseReq req) {
    Uni<School> schoolUni = schoolService.findById(req.school().id());

    return schoolUni.flatMap(school -> update(id, req, school));
  }

  private Uni<Course> update(String id, CourseReq req, School school) {
    Uni<Course> uniExtCourse = findById(id);
    Uni<Boolean> uniNameIsUsed = checkIfNameIsUsed(id, req.name(), school);

    return Panache.withTransaction(() ->
        uniNameIsUsed.flatMap(nameInUsed -> {
          if (nameInUsed) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Name already in used"));
          }

          return uniExtCourse.flatMap(extCourse  -> {
            extCourse.name = req.name();
            extCourse.description = req.description();
            extCourse.school = school;
            return Panache.withTransaction(extCourse::persist);
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

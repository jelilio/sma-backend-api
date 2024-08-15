package io.github.jelilio.smbackend.usermanager.service;

import io.github.jelilio.smbackend.common.dto.CourseReq;
import io.github.jelilio.smbackend.common.utils.Paged;
import io.github.jelilio.smbackend.usermanager.entity.Course;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CourseService {
  Uni<Course> findById(String id);

  Uni<List<Course>> findAll();

  Uni<Paged<Course>> findAll(int size, int index);

  Uni<Course> create(CourseReq req);

  Uni<Course> findOrCreate(String name, School school);

  Uni<Course> update(String id, CourseReq req);

  Uni<Void> delete(String id);
}

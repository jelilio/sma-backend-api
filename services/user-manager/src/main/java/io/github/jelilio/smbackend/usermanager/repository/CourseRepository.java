package io.github.jelilio.smbackend.usermanager.repository;

import io.github.jelilio.smbackend.usermanager.entity.Course;
import io.github.jelilio.smbackend.usermanager.entity.School;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CourseRepository implements PanacheRepository<Course> {
  public Uni<Course> findById(String id) {
    return find("id = ?1", UUID.fromString(id)).firstResult();
  }

  public Uni<Long> countByNameAndSchool(String name, School school) {
    return Course.count("name = ?1 and school = ?2", name, school);
  }

  public Uni<Long> countByNameAndSchoolButNotId(String id, String name, School school) {
    return Course.count("id != ?1 and name = ?2 and school = ?3", UUID.fromString(id), name, school);
  }

  public Uni<Course> findByName(String name, School school) {
    return Course.find("name = ?1 and school = ?2", name, school).firstResult();
  }
}
